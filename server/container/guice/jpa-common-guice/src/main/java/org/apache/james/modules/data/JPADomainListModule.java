/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.modules.data;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.jpa.JPADomainList;
import org.apache.james.domainlist.lib.DomainListConfiguration;
import org.apache.james.lifecycle.api.Configurable;
import org.apache.james.server.core.configuration.ConfigurationProvider;
import org.apache.james.utils.ConfigurationPerformer;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class JPADomainListModule extends AbstractModule {

    @Override
    public void configure() {
        bind(JPADomainList.class).in(Scopes.SINGLETON);
        bind(DomainList.class).to(JPADomainList.class);
        Multibinder.newSetBinder(binder(), ConfigurationPerformer.class).addBinding().to(JPADomainListConfigurationPerformer.class);
    }

    @Provides
    @Singleton
    public DomainListConfiguration provideDomainListConfiguration(ConfigurationProvider configurationProvider) {
        try {
            return DomainListConfiguration.from(configurationProvider.getConfiguration("domainlist"));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Singleton
    public static class JPADomainListConfigurationPerformer implements ConfigurationPerformer {

        private final DomainListConfiguration configuration;
        private final JPADomainList jpaDomainList;

        @Inject
        public JPADomainListConfigurationPerformer(DomainListConfiguration configuration, JPADomainList jpaDomainList) {
            this.configuration = configuration;
            this.jpaDomainList = jpaDomainList;
        }

        @Override
        public void initModule() {
            try {
                jpaDomainList.configure(configuration);
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<Class<? extends Configurable>> forClasses() {
            return ImmutableList.of(JPADomainList.class);
        }
    }
}
