package com.eyes.eyesTools.service.email;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.starter.EyesToolsProperties;
import com.eyes.eyesTools.starter.properties.EmailProperties;
import com.eyes.eyesTools.utils.SpringContextUtils;
import com.eyes.eyesTools.utils.UUIDUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件接收类
 * // TODO: 暂时不可用
 * @author eyes
 * @date 2023/1/10 14:35
 */
@Slf4j
public class EmailReceiver {
  private final String emailHost;             // 发送/接收邮件的主机
  private final String transportType;         // 发送/接收邮件的协议
  private final String toEmail;             // 发送/接收邮件的邮箱
  private final String authCode;              // 发送/接收的邮箱授权码

  // 定时接收邮件任务容器
  private Set<String> receiveKeySet = null;

  // 自动装配时调用
  public EmailReceiver() throws CustomException {
    EmailProperties.Receiver config = SpringContextUtils.getBean(EyesToolsProperties.class).getEmail().getReceiver();
    if (!config.isEnabled()) {
      throw new CustomException("Need to configure and enable the Email Receiver service first");
    }
    this.emailHost = config.getHost();
    this.transportType = config.getTransportType();
    this.toEmail = config.getToEmail();
    this.authCode = config.getAuthCode();
  }

  // 需要创建多个不同的Receiver时调用
  public EmailReceiver(String emailHost, String transportType, String toEmail, String authCode) {
    this.emailHost = emailHost;
    this.transportType = transportType;
    this.toEmail = toEmail;
    this.authCode = authCode;
  }

  /**
   * 接收所有邮件
   * TODO: 有bug，接收的不是全部邮件
   * @return List<ImapEmailInfo>
   */
  public List<ImapEmailInfo> receiveAllEmail() {
    List<ImapEmailInfo> emails = new ArrayList<>();
    try {
      // 初始化默认参数
      Properties props = new Properties();
      props.setProperty("mail.host", emailHost);
      props.setProperty("mail.store.protocol", this.transportType);
      props.setProperty("mail.user", toEmail);

      // 获取Store对象
      Session session = Session.getInstance(props);
      session.setDebug(false);
      Store store = session.getStore();
      store.connect(null, authCode);

      // 获取收件箱内容
      Folder folder = store.getFolder("INBOX");

      // 设置对邮件帐户的访问权限
      folder.open(Folder.READ_WRITE);

      // 得到邮箱帐户中的所有邮件
      Message[] messages = folder.getMessages();
      parseEmail(emails, messages);

      // 关闭邮件夹对象与连接
      folder.close(false);
      store.close();
    } catch (Exception e) {
      log.error("failed to receive mail");
      e.printStackTrace();
    }
    return emails;
  }

  /**
   * 接收未读邮件
   * TODO：有bug，接收的还是全部邮件
   * @return List<ImapEmailInfo>
   */
  public List<ImapEmailInfo> receiveEmail() {
    List<ImapEmailInfo> emails = new ArrayList<>();
    try {
      // 初始化默认参数
      Properties props = new Properties();
      props.setProperty("mail.host", emailHost);
      props.setProperty("mail.store.protocol", this.transportType);
      props.setProperty("mail.user", toEmail);

      // 获取Store对象
      Session session = Session.getInstance(props);
      session.setDebug(false);
      Store store = session.getStore();
      store.connect(null, authCode);

      // 获取收件箱内容
      Folder folder = store.getFolder("INBOX");

      // 设置对邮件帐户的访问权限
      folder.open(Folder.READ_WRITE);

      // 得到邮箱帐户中的所有未读邮件
      Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN),false));
      parseEmail(emails, messages);

      // 关闭邮件夹对象与连接
      folder.close(true);
      store.close();
    } catch (Exception e) {
      log.error("failed to receive mail");
      e.printStackTrace();
    }
    return emails;
  }

  /**
   * 定时收取未读邮件（5min收取一次）
   * TODO：未测试
   * @param emailReceivingScheme 收取邮件回调函数
   * @return 任务key
   */
  public String receiveEmailTask(EmailReceivingScheme emailReceivingScheme) {
    return receiveEmailTask(5 * 60L, emailReceivingScheme);
  }

  /**
   * 定时收取未读邮件
   * TODO: 未测试
   * @param interval 收取邮件间隔
   * @param emailReceivingScheme 收取邮件回调函数
   * @return 任务key
   */
  public String receiveEmailTask(Long interval, EmailReceivingScheme emailReceivingScheme) {
    // 初始化默认参数
    Properties props = new Properties();
    props.setProperty("mail.host", emailHost);
    props.setProperty("mail.store.protocol", this.transportType);
    props.setProperty("mail.user", toEmail);

    // 生成key
    if (Objects.isNull(receiveKeySet)) {
      receiveKeySet = new HashSet<>();
    }
    String key = UUIDUtils.getUUid();
    receiveKeySet.add(key);

    Runnable r = () -> {
      try {
        while (receiveKeySet.contains(key)) {
          // 获取Store对象
          Session session = Session.getInstance(props);
          session.setDebug(false);
          Store store = session.getStore();
          store.connect(null, authCode);

          // 获取收件箱内容
          Folder folder = store.getFolder("INBOX");

          // 设置对邮件帐户的访问权限
          folder.open(Folder.READ_WRITE);

          // 得到邮箱帐户中的所有邮件
          List<ImapEmailInfo> emails = new ArrayList<>();
          Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN),false));
          parseEmail(emails, messages);
          emailReceivingScheme.dealEmail(emails);

          // 关闭邮件夹对象与连接
          folder.close(false);
          store.close();

          // 线程沉睡
          Thread.sleep(interval);
        }
      } catch (Exception e) {
        log.error("failed to receive mail");
        e.printStackTrace();
      }
    };
    new Thread(r).start();

    return key;
  }

  /**
   * 清除定时收取邮件任务
   * @param key 任务key
   * @return boolean
   */
  public boolean clearReceiveEmailTask(String key) {
    if (Objects.isNull(receiveKeySet)) {
      throw new RuntimeException("No mail collection task");
    }
    return receiveKeySet.remove(key);
  }

  /*
   ****************************************************************************************
   *                                    辅助函数
   ****************************************************************************************
   */

  // 解析邮件并返回
  private static void parseEmail(List<ImapEmailInfo> result,Message[] messages) throws Exception {
    if (messages == null || messages.length == 0){
      log.info("No resolvable messages");
      return;
    }
    for(Message message : messages){
      MimeMessage msg = (MimeMessage) message;
      ImapEmailInfo emailInfo = new ImapEmailInfo();
      emailInfo.setSubject(MimeUtility.decodeText(msg.getSubject()));
      emailInfo.setSender(getSenderAddress(msg));
      StringBuffer content = new StringBuffer();
      String contentType = msg.getContentType();
      getHtmlContent(msg, content, contentType.toLowerCase().startsWith("text/plain"));
      emailInfo.setContent(content.toString());
      result.add(emailInfo);
    }
  }

  // 获取邮件内的HTML内容
  private static String getSenderAddress(MimeMessage msg) throws Exception {
    Address[] froms = msg.getFrom();
    if (froms.length < 1)
      throw new MessagingException("No sender");
    InternetAddress address = (InternetAddress) froms[0];
    if(address != null){
      return address.getAddress();
    }
    return null;
  }


  // 获取邮件内的HTML内容
  private static void getHtmlContent(
      Part part, StringBuffer content, boolean plainFlag) throws MessagingException, IOException {
    // 如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
    boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
    if (part.isMimeType("text/html") && !isContainTextAttach && !plainFlag) {
      content.append(MimeUtility.decodeText(part.getContent().toString()));
    } else if (part.isMimeType("text/plain") && !isContainTextAttach && plainFlag) {
      content.append(part.getContent().toString());
    } else if (part.isMimeType("message/rfc822")) {
      getHtmlContent((Part) part.getContent(), content, plainFlag);
    } else if (part.isMimeType("multipart/*")) {
      Multipart multipart = (Multipart) part.getContent();
      int partCount = multipart.getCount();
      for (int i = 0; i < partCount; i++) {
        BodyPart bodyPart = multipart.getBodyPart(i);
        getHtmlContent(bodyPart, content, plainFlag);
      }
    }
  }
}
