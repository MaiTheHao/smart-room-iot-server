package com.iviet.ivshs.core.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.iviet.ivshs.core.properties.FirebaseSDKProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FirebaseSDKConfig {

  private final FirebaseSDKProperties firebaseSDKProperties;

  @Bean
  public GoogleCredentials googleCredentials() {
    try (FileInputStream serviceAccount = new FileInputStream(firebaseSDKProperties.getServiceAccountKeyPath())) {
      return GoogleCredentials.fromStream(serviceAccount);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load Firebase credentials", e);
    }
  }

  @Bean
  public FirebaseApp firebaseApp(GoogleCredentials credentials) {
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build();
    return FirebaseApp.initializeApp(options);
  }

  @Bean
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}

