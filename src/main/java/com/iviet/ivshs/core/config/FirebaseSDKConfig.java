package com.iviet.ivshs.core.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.iviet.ivshs.core.properties.FirebaseSDKProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FirebaseSDKConfig {

  private final FirebaseSDKProperties firebaseSDKProperties;

  @Bean
  public FirebaseApp firebaseApp() {
    try (FileInputStream serviceAccount = new FileInputStream(firebaseSDKProperties.getServiceAccountKeyPath())) {
      return FirebaseApp.initializeApp(
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build());
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Firebase Admin SDK", e);
    }
  }
}
