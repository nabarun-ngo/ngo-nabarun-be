//module nabarun.infra{
//    opens ngo.nabarun.app.infra.boot to spring.core;
//    opens ngo.nabarun.app.infra.core.entity to spring.core;
//	exports ngo.nabarun.app.infra.core.entity to org.mongodb.bson;
//	exports ngo.nabarun.app.infra.boot to spring.beans,spring.context;
//	exports ngo.nabarun.app.infra.serviceimpl to spring.beans;
//	requires spring.data.commons;
//	requires spring.data.mongodb;
//	requires spring.context;
//	requires lombok;
//	requires com.fasterxml.jackson.annotation;
//	requires spring.core;
//	requires org.mongodb.driver.core;
//	requires org.mongodb.driver.sync.client;
//	requires spring.beans;
//	requires org.mongodb.bson;
//	requires  nabarun.util;
//	requires spring.tx;
//
//}