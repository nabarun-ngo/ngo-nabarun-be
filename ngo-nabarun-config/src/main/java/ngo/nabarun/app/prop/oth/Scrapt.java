package ngo.nabarun.app.prop.oth;

//import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
//import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.bson.codecs.configuration.CodecRegistry;
//import org.bson.codecs.pojo.PojoCodecProvider;
//
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;

public class Scrapt {
	
//	@SuppressWarnings("unused")
//	private Map<String, Object> getPropertyMapFromDatabase(String connUri, DBType type) {
//		Map<String, Object> propertySource = new HashMap<>();
//		switch (type) {
//		case MONGO: {
//			ConnectionString connectionString = new ConnectionString(connUri);
//			CodecRegistry pojoCodecRegistry = fromProviders(
//					PojoCodecProvider.builder().register(DBConfig.class).automatic(true).build());
//			CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
//					pojoCodecRegistry);
//			MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
//					.codecRegistry(codecRegistry).build();
//			MongoClient client = MongoClients.create(clientSettings);
//			FindIterable<DBConfig> configs = client.getDatabase(connectionString.getDatabase())
//					.getCollection("", DBConfig.class).find();
//			for (DBConfig dbConfig : configs) {
//				if (dbConfig.isActive()) {
//					try {
//						if (DBConfigParamType.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.BOOLEAN) {
//							propertySource.put(dbConfig.getProperty_key(),
//									Boolean.valueOf(dbConfig.getProperty_value()));
//						} else if (DBConfigParamType
//								.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.DOUBLE) {
//							propertySource.put(dbConfig.getProperty_key(),
//									Double.valueOf(dbConfig.getProperty_value()));
//						} else if (DBConfigParamType
//								.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.INTEGER) {
//							propertySource.put(dbConfig.getProperty_key(),
//									Integer.valueOf(dbConfig.getProperty_value()));
//						} else {
//							propertySource.put(dbConfig.getProperty_key(), dbConfig.getProperty_value());
//						}
//						//System.out.println(dbConfig.getProperty_key() + " -> " + dbConfig.getProperty_value());
//						//log.debug("Property '"+dbConfig.getProperty_key()+"' has been initialized from DB.");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			client.close();
//			break;
//		}
//		default:
//			break;
//		}
//		return propertySource;
//	}
}
