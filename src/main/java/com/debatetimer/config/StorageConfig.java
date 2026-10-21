package com.debatetimer.config;

import com.debatetimer.client.storage.IconStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IconStorageProperties.class)
public class StorageConfig {

}
