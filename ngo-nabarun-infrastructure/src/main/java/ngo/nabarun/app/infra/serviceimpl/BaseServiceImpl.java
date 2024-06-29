package ngo.nabarun.app.infra.serviceimpl;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.CryptUtil;
import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;
import ngo.nabarun.app.infra.core.repo.CustomFieldRepository;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;

@Service
public class BaseServiceImpl {
	
	@Autowired
	private CustomFieldRepository fieldRepository;
	
	@Autowired
	protected GenericPropertyHelper propertyHelper;
	
	protected FieldDTO addOrUpdateCustomField(FieldDTO fieldDTO) {
		CustomFieldEntity existingField = fieldRepository.findBySourceAndFieldKey(fieldDTO.getFieldSource(), fieldDTO.getFieldKey().name()).orElseGet(()->new CustomFieldEntity());

		CustomFieldEntity field = new CustomFieldEntity();
		field.setFieldDescription(fieldDTO.getFieldDescription());
		field.setFieldKey(fieldDTO.getFieldKey().name());
		field.setFieldName(fieldDTO.getFieldName());
		field.setFieldType(fieldDTO.getFieldType());
		field.setFieldValue(fieldDTO.getFieldValue());
		if(fieldDTO.isEncrypted()) {
			field.setEncrypted(true);
			IvParameterSpec iv = CryptUtil.generateIv();
			String salt = UUID.randomUUID().toString();
			field.setEncryptionIV(Base64.getEncoder().encodeToString(iv.getIV()));
			field.setEncryptionSalt(salt);
			SecretKey secretKeyObj;
			try {
				secretKeyObj = CryptUtil.getKeyFromPassword(propertyHelper.getAppSecret(), salt);
				field.setFieldValue(CryptUtil.encrypt(fieldDTO.getFieldValue(), secretKeyObj, iv));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		field.setSource(fieldDTO.getFieldSource());
		field.setSourceType(fieldDTO.getFieldSourceType().name());
		field.setHidden(fieldDTO.isHidden());
		if (existingField.getId() == null) {
			field.setId(UUID.randomUUID().toString());
		}

		CommonUtils.copyNonNullProperties(field, existingField);
		field=fieldRepository.save(existingField);
		return InfraDTOHelper.convertToFieldDTO(field,propertyHelper.getAppSecret());
	}
}
