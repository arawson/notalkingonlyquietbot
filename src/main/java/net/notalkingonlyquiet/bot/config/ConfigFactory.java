package net.notalkingonlyquiet.bot.config;

import com.moandjiezana.toml.Toml;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.io.File;


//TODO: implement so that @Value annotations can be used
//may want to look at https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/beans/factory/config/YamlMapFactoryBean.html
//and https://stackoverflow.com/questions/28303758/how-to-use-yamlpropertiesfactorybean-to-load-yaml-files-using-spring-framework-4
@Component //hopefully it pulls it in
public final class ConfigFactory implements FactoryBean<Config> {
    private Config config;

    @Override
    public Config getObject() throws Exception {
        if (config == null) {
            //TODO: don't hardcode the toml path anymore
            Toml toml = new Toml().read(new File("./config.toml"));
            config = toml.to(Config.class);
        }

        return config;
    }

    @Override
    public Class<Config> getObjectType() {
        return Config.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
