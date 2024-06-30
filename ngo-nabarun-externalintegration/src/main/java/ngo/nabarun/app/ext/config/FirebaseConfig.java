package ngo.nabarun.app.ext.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;


@Configuration
public class FirebaseConfig {
	
	@Autowired
	private GenericPropertyHelper propertyHelper;
	
	@Bean("firebaseApp")
    FirebaseApp createFireBaseApp() {
		FirebaseOptions options;
		try {
			String jsonCredential=propertyHelper.getFirebaseCredential();
			options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(jsonCredential.getBytes())))
					.build();
	        if(FirebaseApp.getApps().isEmpty()) {
		        return FirebaseApp.initializeApp(options);
	        }
	        
	        return FirebaseApp.getInstance();

		} catch (IOException e) {
			e.printStackTrace();
			return FirebaseApp.getInstance();
		}

    }
}