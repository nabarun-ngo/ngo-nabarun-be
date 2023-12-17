package ngo.nabarun.app.api.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AuthorizationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.RefDataType;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,value = "/api/common")
@SecurityRequirement(name = "nabarun_auth")
public class CommonController {
	
	@Autowired
	private ICommonBL commonBL;

	@PostMapping(value="/document/uploadDocuments",consumes="multipart/form-data")
	public ResponseEntity<SuccessResponse<Void>> uploadDocuments(
			@RequestParam String docIndexId,
			@RequestParam DocumentIndexType docIndexType,
			@RequestParam MultipartFile[] files
			) throws Exception {
		commonBL.uploadDocuments(files,docIndexId,docIndexType);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@GetMapping(value="/document/downloadDocument/{id}",produces = MediaType.ALL_VALUE)
	public ResponseEntity<Object> downloadDocument(
			@PathVariable String id,
			@RequestParam(required = false) boolean asURL
			) throws Exception {
		URL url=commonBL.getDocumentUrl(id);
		if(asURL) {
			return new ResponseEntity<Object>(new SuccessResponse<URL>().payload(url), HttpStatus.OK);
		}else {
			return new ResponseEntity<Object>(new UrlResource(url), HttpStatus.OK);
		}
	}
	
	@DeleteMapping(value="/document/deleteDocument/{id}")
	public ResponseEntity<SuccessResponse<Void>> deleteDocument(
			@PathVariable String id
			) throws Exception {
		commonBL.deleteDocument(id);
		return new SuccessResponse<Void>().get(HttpStatus.OK);

	}
	
	@PostMapping(value="/general/clearCache")
	public ResponseEntity<SuccessResponse<Void>> clearCache(@RequestBody List<String> names) throws Exception {
		commonBL.clearSystemCache(names);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@PostMapping(value="/authorization/createAuthorizationUrl")
	public ResponseEntity<SuccessResponse<String>> generateAuthorizationUrl(@RequestBody AuthorizationDetail authDetail) throws Exception {
		return new SuccessResponse<String>().payload(commonBL.generateAuthorizationUrl(authDetail)).get(HttpStatus.OK);
	}
	
	@PostMapping(value="/getReferenceData")
	public ResponseEntity<SuccessResponse<Map<String,List<KeyValue>>>> getReferenceData(@RequestBody(required = false) List<RefDataType> names) throws Exception {
		return new SuccessResponse<Map<String,List<KeyValue>>>().payload(commonBL.getReferenceData(names)).get(HttpStatus.OK);
	}
	
}
