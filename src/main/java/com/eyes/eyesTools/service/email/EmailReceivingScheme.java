package com.eyes.eyesTools.service.email;

import java.util.List;

/**
 * @author eyes
 */
public interface EmailReceivingScheme {
  void dealEmail(List<ImapEmailInfo> email);
}
