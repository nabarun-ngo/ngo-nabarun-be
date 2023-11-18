package ngo.nabarun.app.infra.config;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.mongodb.client.MongoClient;

import java.util.List;
/**
 * 
 * Class to configure MONGODB Soft delete
 */

public class MongoDBSoftDeleteTemplate extends MongoTemplate {

	public MongoDBSoftDeleteTemplate(MongoTemplate mongoTemplate) {
		super(mongoTemplate.getMongoDatabaseFactory());
	}

	public MongoDBSoftDeleteTemplate(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

	public MongoDBSoftDeleteTemplate(MongoDatabaseFactory mongoDbFactory) {
		super(mongoDbFactory);
	}

	public MongoDBSoftDeleteTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter mongoConverter) {
		super(mongoDbFactory, mongoConverter);
	}
	

	/**
	 * Creating criteria for soft deleted
	 */
	private Criteria notDeleted() {
		return Criteria.where("deleted").in(null,false);
	}
	
	/**
	 * Creating criteria for soft deleted
	 */
	private Criteria notDraft() {
		return Criteria.where("draft").in(null,false);
	}
	/**
	 * Overriding find method, this will filter all soft deleted items, like findAll
	 */
	@Override
	public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
		Assert.notNull(query, "Query must not be null!");
		Assert.notNull(collectionName, "CollectionName must not be null!");
		Assert.notNull(entityClass, "EntityClass must not be null!");
		query.addCriteria(notDeleted());
		query.addCriteria(notDraft());
		return super.find(query, entityClass, collectionName);
	}

	@Nullable
	@Override
	public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
		Assert.notNull(query, "Query must not be null!");
		Assert.notNull(entityClass, "EntityClass must not be null!");
		Assert.notNull(collectionName, "CollectionName must not be null!");
		query.addCriteria(notDeleted());

		return super.findOne(query, entityClass, collectionName);
	}

	@Override
	public boolean exists(Query query, @Nullable Class<?> entityClass, String collectionName) {
		if (query == null) {
			throw new InvalidDataAccessApiUsageException("Query passed in to exist can't be null");
		}

		query.addCriteria(notDeleted());

		return super.exists(query, entityClass, collectionName);
	}

}