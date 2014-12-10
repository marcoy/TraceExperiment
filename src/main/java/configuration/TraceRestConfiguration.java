package configuration;

import net.matlux.NreplServerSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceRestConfiguration {
    @Bean
    public NreplServerSpring jvmBreakGlass() {
        return new NreplServerSpring(50000, true, true, true, true);
    }
}
