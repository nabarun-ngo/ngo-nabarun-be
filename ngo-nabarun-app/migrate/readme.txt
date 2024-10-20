## MongoDB Migration Tool ##

mongosh must be installed in the local system 

1. First Authenticate MongoDB connection where you want to migrate.
	mongosh <DB CONNECTION URL>
	
2. In config.json update source and destination DB names

3. Then load the migrate.js file
	load('migrate.js')
	