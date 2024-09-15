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
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.enums.RequestType;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/api/common")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class CommonController {

	@Autowired
	private ICommonBL commonBL;

	@PostMapping(value = "/document/uploadDocuments", consumes = "multipart/form-data")
	public ResponseEntity<SuccessResponse<Void>> uploadDocuments(@RequestParam String docIndexId,
			@RequestParam DocumentIndexType docIndexType, @RequestParam MultipartFile[] files) throws Exception {
		commonBL.uploadDocuments(files, docIndexId, docIndexType);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@PostMapping(value = "/document/uploadBase64Documents")
	public ResponseEntity<SuccessResponse<Void>> uploadDocuments(@RequestParam String docIndexId,
			@RequestParam DocumentIndexType docIndexType, @RequestBody List<DocumentDetailUpload> files)
			throws Exception {
		commonBL.uploadDocuments(files, docIndexId, docIndexType);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@GetMapping(value = "/document/downloadDocument/{id}", produces = MediaType.ALL_VALUE)
	public ResponseEntity<Object> downloadDocument(@PathVariable String id,
			@RequestParam(required = false) boolean asURL) throws Exception {
		URL url = commonBL.getDocumentUrl(id);
		if (asURL) {
			return new ResponseEntity<Object>(new SuccessResponse<URL>().payload(url), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(new UrlResource(url), HttpStatus.OK);
		}
	}

	@DeleteMapping(value = "/document/deleteDocument/{id}")
	public ResponseEntity<SuccessResponse<Void>> deleteDocument(@PathVariable String id) throws Exception {
		commonBL.deleteDocument(id);
		return new SuccessResponse<Void>().get(HttpStatus.OK);

	}

	@PostMapping(value = "/general/clearCache")
	public ResponseEntity<SuccessResponse<Void>> clearCache(@RequestBody List<String> names) throws Exception {
		commonBL.clearSystemCache(names);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@PostMapping(value = "/authorization/createAuthorizationUrl")
	public ResponseEntity<SuccessResponse<String>> generateAuthorizationUrl(@RequestBody AuthorizationDetail authDetail)
			throws Exception {
		return new SuccessResponse<String>().payload(commonBL.generateAuthorizationUrl(authDetail)).get(HttpStatus.OK);
	}

	
	@GetMapping(value = "/getReferenceData")
	public ResponseEntity<SuccessResponse<Map<String, List<KeyValue>>>> getReferenceData(
			@RequestParam(required = false) List<RefDataType> names,
			@RequestParam(required = false) DonationType donationType,
			@RequestParam(required = false) DonationStatus currentDonationStatus,
			@RequestParam(required = false) String countryCode,
			@RequestParam(required = false) String stateCode,
			@RequestParam(required = false) RequestType workflowType
			) throws Exception {
		Map<String, String> options= new HashMap<>();
		if(donationType != null) {
			options.put("donationType", donationType.name());
		}
		if(currentDonationStatus != null) {
			options.put("currentDonationStatus", currentDonationStatus.name());
		}
		if(countryCode != null) {
			options.put("countryCode", countryCode);
		}
		if(stateCode != null) {
			options.put("stateCode", stateCode);
		}
		if(workflowType != null) {
			options.put("workflowType", workflowType.name());
		}
		return new SuccessResponse<Map<String, List<KeyValue>>>()
				.payload(commonBL.getReferenceData(names,options))
				.get(HttpStatus.OK); 
	}
	
	@GetMapping(value = "/getNotifications")
	public ResponseEntity<SuccessResponse<Paginate<Map<String,String>>>> getNotification(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize) throws Exception {
		Paginate<Map<String,String>> notifications=commonBL.getNotifications(pageIndex,pageSize);
		return new SuccessResponse<Paginate<Map<String,String>>>().payload(notifications).get(HttpStatus.OK);
	}

	
	@PostMapping(value = "/manageNotification")
	public ResponseEntity<SuccessResponse<Void>> manageNotification(
			@RequestParam(required = true) String action ,
			@RequestBody Map<String,Object>  body) throws Exception {
		commonBL.manageNotification(action, body);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
}
