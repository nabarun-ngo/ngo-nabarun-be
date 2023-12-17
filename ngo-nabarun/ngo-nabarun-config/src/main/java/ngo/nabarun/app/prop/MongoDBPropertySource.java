package ngo.nabarun.app.prop;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

public class MongoDBPropertySource implements DBPropertySource {

	private static final String COLLECTION_DB_CONFIG = "db_config";
	private MongoClient client;
	private ConnectionString connectionString;

	public MongoDBPropertySource(String connURI) throws Exception {
		connectionString = new ConnectionString(connURI);
		MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();
		client = MongoClients.create(clientSettings);
	}

	@Override
	public void close() {
		client.close();
	}

	@Override
	public void addOrUpdateProperty(String key, Object value, String description, boolean encrypted, String secretKey)
			throws Exception {
		if (encrypted && secretKey == null) {
			throw new Exception("secretkey must be set when encrypted is true;");
		}
		
		MongoCollection<Document> collection = client.getDatabase(connectionString.getDatabase())
				.getCollection(COLLECTION_DB_CONFIG);
		Bson filter = Filters.eq("property_key", key.toUpperCase());
		UpdateOptions options = new UpdateOptions().upsert(true);
		List<Bson> update= new ArrayList<>();
		update.add(Updates.set("property_key", key.toUpperCase()));
		update.add(Updates.set("property_type", value.getClass().getSimpleName()));
		if (encrypted) {
			IvParameterSpec iv = CryptUtil.generateIv();
			String salt = UUID.randomUUID().toString();
			SecretKey secretKeyObj = CryptUtil.getKeyFromPassword(secretKey, salt);
			update.add(Updates.set("iv", Base64.getEncoder().encodeToString(iv.getIV())));
			update.add(Updates.set("salt", salt));
			update.add(Updates.set("property_value", CryptUtil.encrypt(String.valueOf(value), secretKeyObj, iv)));
		} else {
			update.add(Updates.set("property_value", String.valueOf(value)));
		}
		update.add(Updates.set("description", description));
		update.add(Updates.set("encrypted", encrypted));
		update.add(Updates.set("active", true));
		update.add(Updates.set("createdOn", new Date()));
		collection.updateOne(filter, update, options);
	}
	
	

	@Override
	public Map<String, Object> loadProperties(String secretKey) {
		Map<String, Object> propertySource = new HashMap<>();
		FindIterable<Document> documents = client.getDatabase(connectionString.getDatabase())
				.getCollection(COLLECTION_DB_CONFIG).find();
		for (Document dbConfig : documents) {
			if (dbConfig.getBoolean("active", true)) {
				try {
					String key = dbConfig.getString("property_key");
					boolean isEncrypted = dbConfig.getBoolean("encrypted", false);
					if (isEncrypted && secretKey!=null) {
						
						IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(dbConfig.getString("iv")));
						String salt = dbConfig.getString("salt");
						String value = dbConfig.getString("property_value");
						SecretKey secretKeyObj = CryptUtil.getKeyFromPassword(secretKey, salt);
						propertySource.put(key, CryptUtil.decrypt(value, secretKeyObj, iv));

					} else {
						propertySource.put(key, dbConfig.getString("property_value"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		return propertySource;
	}

	@Override
	public Map<String, Object> loadProperties() {
		return loadProperties(null);
	}
	
	

}
