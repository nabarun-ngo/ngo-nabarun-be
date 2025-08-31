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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.helper.Authority;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.ImportantLinks;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(value = "/api/common")
@SecurityRequirement(name = "nabarun_auth")
@SecurityRequirement(name = "nabarun_auth_apikey")
public class CommonController {

	@Autowired
	private ICommonBL commonBL;
	
	@Operation(summary = "Retrieve list of documents against docIndexType and docIndexId")
	@PreAuthorize(Authority.READ_DOCUMENT_LIST)
	@GetMapping(value = "/document/{docIndexType}/{docIndexId}/list",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<List<DocumentDetail>>> getDocuments(@PathVariable String docIndexId,@PathVariable DocumentIndexType docIndexType)
			throws Exception {
		return new SuccessResponse<List<DocumentDetail>>().payload(commonBL.getDocuments(docIndexId,docIndexType))
				.get(HttpStatus.OK);
	}
	
	@Operation(summary = "Upload document")
	@PostMapping(value = "/document/upload", consumes = "multipart/form-data")
	public ResponseEntity<SuccessResponse<Void>> uploadDocuments(@RequestParam String docIndexId,
			@RequestParam DocumentIndexType docIndexType, @RequestParam MultipartFile[] files) throws Exception {
		commonBL.uploadDocuments(files, docIndexId, docIndexType);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@Operation(summary = "Upload document as base64")
	@PostMapping(value = "/document/uploadbase64",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> uploadDocuments(@RequestBody List<DocumentDetailUpload> files)
			throws Exception {
		commonBL.uploadDocuments(files);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}

	@Operation(summary = "View a document")
	@GetMapping(value = "/document/{id}/view",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<DocumentDetail>> viewDocument(@PathVariable String id) throws Exception {
		DocumentDetail doc=commonBL.getDocument(id);
		return new SuccessResponse<DocumentDetail>().payload(doc).get(HttpStatus.OK);

	}
	
	@Operation(summary = "Download a document")
	@GetMapping(value = "/document/{id}/download")
	public ResponseEntity<Object> downloadDocument(@PathVariable String id) throws Exception {
		DocumentDetail doc = commonBL.getDocument(id);
		UrlResource resource=new UrlResource(doc.getDownloadURL());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@Operation(summary = "Delete a document",description = "Authorities : "+Authority.DELETE_DOCUMENT)
	@PreAuthorize(Authority.DELETE_DOCUMENT)
	@DeleteMapping(value = "/document/{id}/delete",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> deleteDocument(@PathVariable String id) throws Exception {
		commonBL.deleteDocument(id);
		return new SuccessResponse<Void>().get(HttpStatus.OK);

	}

	@Operation(summary = "Retrieve reference field against source")
	@GetMapping(value = "/data/referenceFields",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<List<AdditionalField>>> getReferenceField(@RequestParam String source)
			throws Exception {
		List<AdditionalField> fields = commonBL.getReferenceFields(source);
		return new SuccessResponse<List<AdditionalField>>().payload(fields).get(HttpStatus.OK);
	}

	@Operation(summary = "Retrieve reference data with filter")
	@GetMapping(value = "/data/referenceData",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Map<String, List<KeyValue>>>> getReferenceData(
			@RequestParam(required = false) List<RefDataType> names,
			@RequestParam(required = false) DonationType donationType,
			@RequestParam(required = false) DonationStatus currentDonationStatus,
			@RequestParam(required = false) String countryCode, @RequestParam(required = false) String stateCode,
			@RequestParam(required = false) RequestType workflowType) throws Exception {
		Map<String, String> options = new HashMap<>();
		if (donationType != null) {
			options.put("donationType", donationType.name());
		}
		if (currentDonationStatus != null) {
			options.put("currentDonationStatus", currentDonationStatus.name());
		}
		if (countryCode != null) {
			options.put("countryCode", countryCode);
		}
		if (stateCode != null) {
			options.put("stateCode", stateCode);
		}
		if (workflowType != null) {
			options.put("workflowType", workflowType.name());
		}
		return new SuccessResponse<Map<String, List<KeyValue>>>().payload(commonBL.getReferenceData(names, options))
				.get(HttpStatus.OK);
	}

	@Operation(summary = "Retrieve notifications for logged in user")
	@GetMapping(value = "/notification/list",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Paginate<Map<String, String>>>> getNotification(
			@RequestParam(required = false) Integer pageIndex, @RequestParam(required = false) Integer pageSize)
			throws Exception {
		Paginate<Map<String, String>> notifications = commonBL.getNotifications(pageIndex, pageSize);
		return new SuccessResponse<Paginate<Map<String, String>>>().payload(notifications).get(HttpStatus.OK);
	}

	@Operation(summary = "Manage notification for logged in user")
	@PostMapping(value = "/notification/manage",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SuccessResponse<Void>> manageNotification(@RequestParam(required = true) String action,
			@RequestBody Map<String, Object> body) throws Exception {
		AuthenticatedUser user=SecurityUtils.getAuthUser();
		commonBL.manageNotification(user,action, body);
		return new SuccessResponse<Void>().get(HttpStatus.OK);
	}
	
	@Operation(summary = "Retrieve useful links")
	@GetMapping("/useful-links")
	public ResponseEntity<SuccessResponse<ImportantLinks>> getUsefulLinks() throws Exception {
		return new SuccessResponse<ImportantLinks>().payload(commonBL.getUsefulLinks()).get(HttpStatus.OK);
	}
}