package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class FirebaseSDKProperties {

  @Value("${app.firebase.service-account-key-path}")
  private String serviceAccountKeyPath;
}
