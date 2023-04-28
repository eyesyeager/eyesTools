package com.eyes.eyesTools.service.email;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.starter.EyesToolsProperties;
import com.eyes.eyesTools.starter.properties.EmailProperties;
import com.eyes.eyesTools.utils.SpringContextUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;

/***
 * 邮件发送类
 * TODO: 把错误抛出来！！！
 * 有两种实例化方法
 *     1. 在配置文件中配置enabled=true，以及其他参数，项目启动时会自动装配进容器
 *     2. 利用EmailSender(String, String, String, String, String)构造方法
 * @author eyes
 */
@Slf4j
public class EmailSender {
    private final String emailHost;             // 发送邮件的主机
    private final String transportType;         // 发送邮件的协议
    private final String fromEmail;             // 发送邮件的邮箱
    private final String authCode;              // 发送的邮箱授权码
    private final String senderName;            // 发件人名称

    /**
     * 自动装配时调用
     * 该构造器只能由SpringBoot调用，不能由程序员调用
     * @throws CustomException 调用该方法前必须配置enabled=true
     */
    public EmailSender() throws CustomException {
        EmailProperties.Sender config = SpringContextUtils.getBean(EyesToolsProperties.class).getEmail().getSender();
        if (!config.isEnabled()) {
            throw new CustomException("Need to configure and enable the Email Sender service first");
        }
        this.emailHost = config.getHost();
        this.transportType = config.getTransportType();
        this.fromEmail = config.getFromEmail();
        this.authCode = config.getAuthCode();
        this.senderName = config.getSenderName();
    }

    /**
     * 需要创建多个不同的Sender时调用
     * 该构造器由程序员调用
     * @param emailHost 发送邮件的主机
     * @param transportType 发送邮件的协议
     * @param fromEmail 发送邮件的邮箱
     * @param authCode 发送的邮箱授权码
     * @param senderName 发件人名称
     */
    public EmailSender(String emailHost, String transportType, String fromEmail, String authCode, String senderName) {
        this.emailHost = emailHost;
        this.transportType = transportType;
        this.fromEmail = fromEmail;
        this.authCode = authCode;
        this.senderName = senderName;
    }

    /**
     * 发送HTML邮件（立即发送、无抄送人/密抄送人）
     * @param toEmail 目标邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendMail(String toEmail, String subject, String content) {
        sendMail(toEmail, subject, content, null, null);
    }

    /**
     * 发送HTML邮件(立即发送)
     * @param toEmail 目标邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc 抄送人
     * @param bcc 密送人
     */
    public void sendMail(String toEmail, String subject, String content, List<String> cc, List<String> bcc) {
        sendMail(toEmail, subject, content, cc, bcc, null);
    }

    /**
     * 发送HTML邮件
     * @param toEmail 目标邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc 抄送人
     * @param bcc 密送人
     * @param date 发件时间(定时发送)
     */
    public void sendMail(String toEmail, String subject, String content, List<String> cc, List<String> bcc, Date date) {
        sendMail(toEmail, subject, content, cc, bcc, date, "text/html");
    }

    /**
     * 发送邮件
     * @param toEmail 目标邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc 抄送人
     * @param bcc 密送人
     * @param date 发件时间(定时发送)
     * @param format 邮件格式
     */
    public void sendMail(String toEmail, String subject, String content, List<String> cc, List<String> bcc, Date date, String format) {
        try {
            // 初始化默认参数
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", this.transportType);
            props.setProperty("mail.host", this.emailHost);
            props.setProperty("mail.user", this.fromEmail);  // 没搞懂这user和from有什么区别，但是两个都得有，而且得相同，不然报错
            props.setProperty("mail.from", this.fromEmail);

            // 创建Message
            Session session = Session.getInstance(props, null);
            session.setDebug(false);  // 开启后有调试信息
            MimeMessage message = new MimeMessage(session);

            // 设置发件人
            String formName = MimeUtility.encodeWord(senderName) + " <" + fromEmail + ">";
            InternetAddress from = new InternetAddress(formName);
            message.setFrom(from);

            // 设置收件人
            InternetAddress to = new InternetAddress(toEmail);
            message.setRecipient(Message.RecipientType.TO, to);

            // 设置抄送人
            if (Objects.nonNull(cc) && !cc.isEmpty()) {
                List<InternetAddress> addresses = new ArrayList<>();
                for (String item: cc) {
                    addresses.add(new InternetAddress(item));
                }
                InternetAddress[] addressesArr = addresses.toArray(new InternetAddress[0]);
                message.setRecipients(Message.RecipientType.CC, addressesArr);
            }

            // 设置密送人
            if (Objects.nonNull(bcc) && !bcc.isEmpty()) {
                List<InternetAddress> addresses = new ArrayList<>();
                for (String item: bcc) {
                    addresses.add(new InternetAddress(item));
                }
                InternetAddress[] addressesArr = addresses.toArray(new InternetAddress[0]);
                message.setRecipients(Message.RecipientType.BCC, addressesArr);
            }

            // 设置邮件主题
            message.setSubject(subject);

            // 设置发件时间
            if (Objects.nonNull(date)) {
                message.setSentDate(date);
            }

            // 设置邮件内容
            message.setContent(content, format + ";charset=UTF-8");

            // 发送邮件
            message.saveChanges();
            Transport transport = session.getTransport();
            transport.connect(null, null, authCode);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            log.error("failed to send mail");
            e.printStackTrace();
        }
    }
}