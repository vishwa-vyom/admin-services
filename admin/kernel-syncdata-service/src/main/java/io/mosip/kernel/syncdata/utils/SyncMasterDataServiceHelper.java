package io.mosip.kernel.syncdata.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.mosip.kernel.clientcrypto.dto.TpmCryptoRequestDto;
import io.mosip.kernel.clientcrypto.dto.TpmCryptoResponseDto;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.syncdata.exception.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.dto.AppAuthenticationMethodDto;
import io.mosip.kernel.syncdata.dto.AppDetailDto;
import io.mosip.kernel.syncdata.dto.AppRolePriorityDto;
import io.mosip.kernel.syncdata.dto.ApplicantValidDocumentDto;
import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.BiometricAttributeDto;
import io.mosip.kernel.syncdata.dto.BiometricTypeDto;
import io.mosip.kernel.syncdata.dto.BlacklistedWordsDto;
import io.mosip.kernel.syncdata.dto.DeviceDto;
import io.mosip.kernel.syncdata.dto.DeviceProviderDto;
import io.mosip.kernel.syncdata.dto.DeviceServiceDto;
import io.mosip.kernel.syncdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.syncdata.dto.DeviceSubTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDto;
import io.mosip.kernel.syncdata.dto.DocumentCategoryDto;
import io.mosip.kernel.syncdata.dto.DocumentTypeDto;
import io.mosip.kernel.syncdata.dto.FoundationalTrustProviderDto;
import io.mosip.kernel.syncdata.dto.GenderDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.IdTypeDto;
import io.mosip.kernel.syncdata.dto.IndividualTypeDto;
import io.mosip.kernel.syncdata.dto.LanguageDto;
import io.mosip.kernel.syncdata.dto.LocationDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.MachineSpecificationDto;
import io.mosip.kernel.syncdata.dto.MachineTypeDto;
import io.mosip.kernel.syncdata.dto.PostReasonCategoryDto;
import io.mosip.kernel.syncdata.dto.ProcessListDto;
import io.mosip.kernel.syncdata.dto.ReasonListDto;
import io.mosip.kernel.syncdata.dto.RegisteredDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterTypeDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingHistoryDto;
import io.mosip.kernel.syncdata.dto.ScreenAuthorizationDto;
import io.mosip.kernel.syncdata.dto.ScreenDetailDto;
import io.mosip.kernel.syncdata.dto.SyncJobDefDto;
import io.mosip.kernel.syncdata.dto.TemplateDto;
import io.mosip.kernel.syncdata.dto.TemplateFileFormatDto;
import io.mosip.kernel.syncdata.dto.TemplateTypeDto;
import io.mosip.kernel.syncdata.dto.TitleDto;
import io.mosip.kernel.syncdata.dto.ValidDocumentDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.AppAuthenticationMethod;
import io.mosip.kernel.syncdata.entity.AppDetail;
import io.mosip.kernel.syncdata.entity.AppRolePriority;
import io.mosip.kernel.syncdata.entity.ApplicantValidDocument;
import io.mosip.kernel.syncdata.entity.Application;
import io.mosip.kernel.syncdata.entity.BiometricAttribute;
import io.mosip.kernel.syncdata.entity.BiometricType;
import io.mosip.kernel.syncdata.entity.BlacklistedWords;
import io.mosip.kernel.syncdata.entity.Device;
import io.mosip.kernel.syncdata.entity.DeviceHistory;
import io.mosip.kernel.syncdata.entity.DeviceProvider;
import io.mosip.kernel.syncdata.entity.DeviceService;
import io.mosip.kernel.syncdata.entity.DeviceSpecification;
import io.mosip.kernel.syncdata.entity.DeviceSubTypeDPM;
import io.mosip.kernel.syncdata.entity.DeviceType;
import io.mosip.kernel.syncdata.entity.DeviceTypeDPM;
import io.mosip.kernel.syncdata.entity.DocumentCategory;
import io.mosip.kernel.syncdata.entity.DocumentType;
import io.mosip.kernel.syncdata.entity.FoundationalTrustProvider;
import io.mosip.kernel.syncdata.entity.Gender;
import io.mosip.kernel.syncdata.entity.Holiday;
import io.mosip.kernel.syncdata.entity.IdType;
import io.mosip.kernel.syncdata.entity.IndividualType;
import io.mosip.kernel.syncdata.entity.Language;
import io.mosip.kernel.syncdata.entity.Location;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.entity.MachineHistory;
import io.mosip.kernel.syncdata.entity.MachineSpecification;
import io.mosip.kernel.syncdata.entity.MachineType;
import io.mosip.kernel.syncdata.entity.ProcessList;
import io.mosip.kernel.syncdata.entity.ReasonCategory;
import io.mosip.kernel.syncdata.entity.ReasonList;
import io.mosip.kernel.syncdata.entity.RegisteredDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenter;
import io.mosip.kernel.syncdata.entity.RegistrationCenterType;
import io.mosip.kernel.syncdata.entity.ScreenAuthorization;
import io.mosip.kernel.syncdata.entity.ScreenDetail;
import io.mosip.kernel.syncdata.entity.Template;
import io.mosip.kernel.syncdata.entity.TemplateFileFormat;
import io.mosip.kernel.syncdata.entity.TemplateType;
import io.mosip.kernel.syncdata.entity.Title;
import io.mosip.kernel.syncdata.entity.UserDetails;
import io.mosip.kernel.syncdata.entity.UserDetailsHistory;
import io.mosip.kernel.syncdata.entity.ValidDocument;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.repository.AppAuthenticationMethodRepository;
import io.mosip.kernel.syncdata.repository.AppDetailRepository;
import io.mosip.kernel.syncdata.repository.AppRolePriorityRepository;
import io.mosip.kernel.syncdata.repository.ApplicantValidDocumentRespository;
import io.mosip.kernel.syncdata.repository.ApplicationRepository;
import io.mosip.kernel.syncdata.repository.BiometricAttributeRepository;
import io.mosip.kernel.syncdata.repository.BiometricTypeRepository;
import io.mosip.kernel.syncdata.repository.BlacklistedWordsRepository;
import io.mosip.kernel.syncdata.repository.DeviceHistoryRepository;
import io.mosip.kernel.syncdata.repository.DeviceProviderRepository;
import io.mosip.kernel.syncdata.repository.DeviceRepository;
import io.mosip.kernel.syncdata.repository.DeviceServiceRepository;
import io.mosip.kernel.syncdata.repository.DeviceSpecificationRepository;
import io.mosip.kernel.syncdata.repository.DeviceSubTypeDPMRepository;
import io.mosip.kernel.syncdata.repository.DeviceTypeDPMRepository;
import io.mosip.kernel.syncdata.repository.DeviceTypeRepository;
import io.mosip.kernel.syncdata.repository.DocumentCategoryRepository;
import io.mosip.kernel.syncdata.repository.DocumentTypeRepository;
import io.mosip.kernel.syncdata.repository.FoundationalTrustProviderRepository;
import io.mosip.kernel.syncdata.repository.GenderRepository;
import io.mosip.kernel.syncdata.repository.HolidayRepository;
import io.mosip.kernel.syncdata.repository.IdTypeRepository;
import io.mosip.kernel.syncdata.repository.IndividualTypeRepository;
import io.mosip.kernel.syncdata.repository.LanguageRepository;
import io.mosip.kernel.syncdata.repository.LocationRepository;
import io.mosip.kernel.syncdata.repository.MachineHistoryRepository;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import io.mosip.kernel.syncdata.repository.MachineSpecificationRepository;
import io.mosip.kernel.syncdata.repository.MachineTypeRepository;
import io.mosip.kernel.syncdata.repository.ProcessListRepository;
import io.mosip.kernel.syncdata.repository.ReasonCategoryRepository;
import io.mosip.kernel.syncdata.repository.ReasonListRepository;
import io.mosip.kernel.syncdata.repository.RegisteredDeviceRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterTypeRepository;
import io.mosip.kernel.syncdata.repository.ScreenAuthorizationRepository;
import io.mosip.kernel.syncdata.repository.ScreenDetailRepository;
import io.mosip.kernel.syncdata.repository.TemplateFileFormatRepository;
import io.mosip.kernel.syncdata.repository.TemplateRepository;
import io.mosip.kernel.syncdata.repository.TemplateTypeRepository;
import io.mosip.kernel.syncdata.repository.TitleRepository;
import io.mosip.kernel.syncdata.repository.UserDetailsHistoryRepository;
import io.mosip.kernel.syncdata.repository.UserDetailsRepository;
import io.mosip.kernel.syncdata.repository.ValidDocumentRepository;
import io.mosip.kernel.syncdata.service.SyncJobDefService;

/**
 * Sync handler masterData service helper
 * 
 * @author Abhishek Kumar
 * @author Srinivasan
 * @since 1.0.0
 */
@Component
public class SyncMasterDataServiceHelper {
	
	private Logger logger = LoggerFactory.getLogger(SyncMasterDataServiceHelper.class);

	@Autowired
	private MapperUtils mapper;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private MachineRepository machineRepository;
	@Autowired
	private MachineTypeRepository machineTypeRepository;
	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;
	@Autowired
	private RegistrationCenterTypeRepository registrationCenterTypeRepository;
	@Autowired
	private TemplateRepository templateRepository;
	@Autowired
	private TemplateFileFormatRepository templateFileFormatRepository;
	@Autowired
	private ReasonCategoryRepository reasonCategoryRepository;
	@Autowired
	private HolidayRepository holidayRepository;
	@Autowired
	private BlacklistedWordsRepository blacklistedWordsRepository;
	@Autowired
	private BiometricTypeRepository biometricTypeRepository;
	@Autowired
	private BiometricAttributeRepository biometricAttributeRepository;
	@Autowired
	private TitleRepository titleRepository;
	@Autowired
	private LanguageRepository languageRepository;
	@Autowired
	private GenderRepository genderTypeRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private DocumentCategoryRepository documentCategoryRepository;
	@Autowired
	private DocumentTypeRepository documentTypeRepository;
	@Autowired
	private IdTypeRepository idTypeRepository;
	@Autowired
	private DeviceSpecificationRepository deviceSpecificationRepository;
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private TemplateTypeRepository templateTypeRepository;
	@Autowired
	private MachineSpecificationRepository machineSpecificationRepository;
	@Autowired
	private DeviceTypeRepository deviceTypeRepository;
	@Autowired
	private ValidDocumentRepository validDocumentRepository;
	@Autowired
	private ReasonListRepository reasonListRepository;
	@Autowired
	private UserDetailsRepository userDetailsRepository;
	@Autowired
	private ApplicantValidDocumentRespository applicantValidDocumentRepository;
	@Autowired
	private IndividualTypeRepository individualTypeRepository;
	@Autowired
	private AppAuthenticationMethodRepository appAuthenticationMethodRepository;
	@Autowired
	private AppDetailRepository appDetailRepository;
	@Autowired
	private AppRolePriorityRepository appRolePriorityRepository;
	@Autowired
	private ScreenAuthorizationRepository screenAuthorizationRepository;
	@Autowired
	private ProcessListRepository processListRepository;
	@Autowired
	private ScreenDetailRepository screenDetailRepository;
	@Autowired
	private SyncJobDefService syncJobDefService;
	@Autowired
	private DeviceProviderRepository deviceProviderRepository;
	@Autowired
	private DeviceServiceRepository deviceServiceRepository;
	@Autowired
	private RegisteredDeviceRepository registeredDeviceRepository;
	@Autowired
	private FoundationalTrustProviderRepository foundationalTrustProviderRepository;
	@Autowired
	private DeviceTypeDPMRepository deviceTypeDPMRepository;
	@Autowired
	private DeviceSubTypeDPMRepository deviceSubTypeDPMRepository;
	@Autowired
	private UserDetailsHistoryRepository userDetailsHistoryRepository;
	@Autowired
	private MachineHistoryRepository machineHistoryRepository;
	@Autowired
	private DeviceHistoryRepository deviceHistoryRepository;

	@Autowired
	private ClientCryptoManagerService clientCryptoManagerService;

	@Value("${mosip.syncdata.tpm.required:false}")
	private boolean isTPMRequired;

	/**
	 * Method to fetch machine details by regCenter id
	 * 
	 * @param regCenterId      registration center id
	 * @param lastUpdated      lastUpdated time-stamp
	 * @param currentTimeStamp current time stamp
	 * 
	 * @return list of {@link MachineDto} list of machine dto
	 */
	@Async
	public CompletableFuture<List<MachineDto>> getMachines(String regCenterId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<Machine> machineDetailList = null;
		List<MachineDto> machineDetailDtoList = new ArrayList<>();
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machineDetailList = machineRepository.findAllLatestCreatedUpdateDeleted(regCenterId, lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.MACHINE_DETAIL_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (!machineDetailList.isEmpty()) {

			// machineDetailDtoList = MapperUtils.mapAll(machineDetailList,
			// MachineDto.class);
			machineDetailList.forEach(machine -> {
				MachineDto responseDto = new MachineDto();
				responseDto.setPublicKey(machine.getPublicKey());
				responseDto.setId(machine.getId());
				responseDto.setIpAddress(machine.getIpAddress());
				responseDto.setIsActive(machine.getIsActive());
				responseDto.setIsDeleted(machine.getIsDeleted());
				responseDto.setKeyIndex(machine.getKeyIndex());
				responseDto.setLangCode(machine.getLangCode());
				responseDto.setMacAddress(machine.getMacAddress());
				responseDto.setMachineSpecId(machine.getMachineSpecId());
				responseDto.setName(machine.getName());
				responseDto.setSerialNum(machine.getSerialNum());
				responseDto.setValidityDateTime(machine.getValidityDateTime());
				machineDetailDtoList.add(responseDto);
			});

		}

		return CompletableFuture.completedFuture(machineDetailDtoList);
	}

	/**
	 * Method to fetch machine type
	 * 
	 * @param regCenterId      registration center id
	 * @param lastUpdated      lastupdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link MachineType}
	 */
	@Async
	public CompletableFuture<List<MachineTypeDto>> getMachineType(String regCenterId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<MachineTypeDto> machineTypeList = null;
		List<MachineType> machineTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machineTypes = machineTypeRepository.findLatestByRegCenterId(regCenterId, lastUpdated, currentTimeStamp);

		} catch (

		DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.MACHINE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (machineTypes != null && !machineTypes.isEmpty())

			machineTypeList = MapperUtils.mapAll(machineTypes, MachineTypeDto.class);

		return CompletableFuture.completedFuture(machineTypeList);

	}

	/**
	 * Method to fetch machine specification
	 * 
	 * @param regCenterId      registration center id
	 * @param lastUpdated      lastupdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link MachineSpecificationDto}
	 */
	@Async
	public CompletableFuture<List<MachineSpecificationDto>> getMachineSpecification(String regCenterId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<MachineSpecification> machineSpecification = null;
		List<MachineSpecificationDto> machineSpecificationDto = null;

		try {
			if (regCenterId != null) {
				if (lastUpdated == null) {
					lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
				}
				machineSpecification = machineSpecificationRepository.findLatestByRegCenterId(regCenterId, lastUpdated,
						currentTimeStamp);

			}
		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (machineSpecification != null && !machineSpecification.isEmpty())

			machineSpecificationDto = MapperUtils.mapAll(machineSpecification, MachineSpecificationDto.class);

		return CompletableFuture.completedFuture(machineSpecificationDto);
	}

	/**
	 * Method to fetch registration center detail.
	 *
	 * @param machineId        machine id
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp the current time stamp
	 * @return list of {@link RegistrationCenterDto}
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterDto>> getRegistrationCenter(String machineId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterDto> registrationCenterList = null;
		List<RegistrationCenter> list = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			list = registrationCenterRepository.findLatestRegistrationCenterByMachineId(machineId, lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (list != null && !list.isEmpty()) {
			registrationCenterList = MapperUtils.mapAll(list, RegistrationCenterDto.class);
		}

		return CompletableFuture.completedFuture(registrationCenterList);
	}

	/**
	 * Method to fetch registration center type
	 * 
	 * @param machineId        machine id
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterTypeDto}
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterTypeDto>> getRegistrationCenterType(String machineId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterTypeDto> registrationCenterTypes = null;
		List<RegistrationCenterType> registrationCenterType = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			registrationCenterType = registrationCenterTypeRepository
					.findLatestRegistrationCenterTypeByMachineId(machineId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (registrationCenterType != null && !registrationCenterType.isEmpty())
			registrationCenterTypes = MapperUtils.mapAll(registrationCenterType, RegistrationCenterTypeDto.class);

		return CompletableFuture.completedFuture(registrationCenterTypes);
	}

	/**
	 * Method to fetch applications
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link ApplicationDto}
	 */
	@Async
	public CompletableFuture<List<ApplicationDto>> getApplications(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<ApplicationDto> applications = null;
		List<Application> applicationList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			applicationList = applicationRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (!(applicationList.isEmpty())) {
			applications = MapperUtils.mapAll(applicationList, ApplicationDto.class);
		}
		return CompletableFuture.completedFuture(applications);
	}

	/**
	 * Method to fetch templates
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link TemplateDto}
	 */
	@Async
	public CompletableFuture<List<TemplateDto>> getTemplates(String moduleId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<TemplateDto> templates = null;
		List<Template> templateList = null;
		try {

			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			templateList = templateRepository.findAllLatestCreatedUpdateDeletedByModule(lastUpdated, currentTimeStamp, moduleId);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (templateList != null && !templateList.isEmpty()) {
			templates = MapperUtils.mapAll(templateList, TemplateDto.class);
		}
		return CompletableFuture.completedFuture(templates);
	}

	/**
	 * Method to fetch template format types
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link TemplateFileFormatDto}
	 */
	@Async
	public CompletableFuture<List<TemplateFileFormatDto>> getTemplateFileFormats(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<TemplateFileFormatDto> templateFormats = null;
		List<TemplateFileFormat> templateTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			templateTypes = templateFileFormatRepository.findAllLatestCreatedUpdateDeleted(lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		templateFormats = MapperUtils.mapAll(templateTypes, TemplateFileFormatDto.class);
		return CompletableFuture.completedFuture(templateFormats);
	}

	/**
	 * Method to fetch reason-category
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link PostReasonCategoryDto}
	 */
	@Async
	public CompletableFuture<List<PostReasonCategoryDto>> getReasonCategory(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<PostReasonCategoryDto> reasonCategories = null;
		List<ReasonCategory> reasons = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			reasons = reasonCategoryRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REASON_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (reasons != null && !reasons.isEmpty()) {
			reasonCategories = MapperUtils.mapAll(reasons, PostReasonCategoryDto.class);
		}
		return CompletableFuture.completedFuture(reasonCategories);
	}

	/**
	 * Method to fetch Reason List
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link ReasonListDto}
	 */
	@Async
	public CompletableFuture<List<ReasonListDto>> getReasonList(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<ReasonListDto> reasonList = null;
		List<ReasonList> reasons = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			reasons = reasonListRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REASON_LIST_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (reasons != null && !reasons.isEmpty())
			reasonList = MapperUtils.mapAll(reasons, ReasonListDto.class);

		return CompletableFuture.completedFuture(reasonList);
	}

	/**
	 * Method to fetch Holidays
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @param machineId        machine id
	 * @return list of {@link HolidayDto}
	 */
	@Async
	public CompletableFuture<List<HolidayDto>> getHolidays(LocalDateTime lastUpdated, String machineId,
			LocalDateTime currentTimeStamp) {
		List<HolidayDto> holidayList = null;
		List<Holiday> holidays = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			holidays = holidayRepository.findAllLatestCreatedUpdateDeletedByMachineId(machineId, lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (holidays != null && !holidays.isEmpty())
			holidayList = mapper.mapHolidays(holidays);

		return CompletableFuture.completedFuture(holidayList);
	}

	/**
	 * Method to fetch blacklisted words
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link BlacklistedWordsDto}
	 */
	@Async
	public CompletableFuture<List<BlacklistedWordsDto>> getBlackListedWords(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<BlacklistedWordsDto> blacklistedWords = null;
		List<BlacklistedWords> words = null;

		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			words = blacklistedWordsRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.BLACKLISTED_WORDS_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (words != null && !words.isEmpty()) {
			blacklistedWords = MapperUtils.mapAll(words, BlacklistedWordsDto.class);
		}

		return CompletableFuture.completedFuture(blacklistedWords);
	}

	/**
	 * Method to fetch biometric types
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link BiometricTypeDto}
	 */
	@Async
	public CompletableFuture<List<BiometricTypeDto>> getBiometricTypes(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<BiometricTypeDto> biometricTypeDtoList = null;
		List<BiometricType> biometricTypesList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			biometricTypesList = biometricTypeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (!(biometricTypesList.isEmpty())) {
			biometricTypeDtoList = MapperUtils.mapAll(biometricTypesList, BiometricTypeDto.class);
		}
		return CompletableFuture.completedFuture(biometricTypeDtoList);
	}

	/**
	 * Method to fetch biometric attributes
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link BiometricAttributeDto}
	 */
	@Async
	public CompletableFuture<List<BiometricAttributeDto>> getBiometricAttributes(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<BiometricAttributeDto> biometricAttrList = null;
		List<BiometricAttribute> biometricAttrs = null;
		try {

			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			biometricAttrs = biometricAttributeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.BIOMETRIC_ATTR_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (biometricAttrs != null && !biometricAttrs.isEmpty()) {
			biometricAttrList = MapperUtils.mapAll(biometricAttrs, BiometricAttributeDto.class);
		}
		return CompletableFuture.completedFuture(biometricAttrList);
	}

	/**
	 * Method to fetch titles
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link TitleDto}
	 */
	@Async
	public CompletableFuture<List<TitleDto>> getTitles(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<TitleDto> titleList = null;
		List<Title> titles = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			titles = titleRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.TITLE_FETCH_EXCEPTION.getErrorCode(), e.getMessage(),
					e);
		}
		if (titles != null && !titles.isEmpty()) {

			titleList = MapperUtils.mapAll(titles, TitleDto.class);
		}
		return CompletableFuture.completedFuture(titleList);

	}

	/**
	 * Method to fetch languages
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link LanguageDto}
	 */
	@Async
	public CompletableFuture<List<LanguageDto>> getLanguages(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<LanguageDto> languageList = null;
		List<Language> languages = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			languages = languageRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.LANGUAGE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (languages != null && !languages.isEmpty()) {
			languageList = MapperUtils.mapAll(languages, LanguageDto.class);
		}
		return CompletableFuture.completedFuture(languageList);
	}

	/**
	 * Method to fetch genders
	 * 
	 * @param lastUpdated      lastUpdated
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link GenderDto}
	 */
	@Async
	public CompletableFuture<List<GenderDto>> getGenders(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<GenderDto> genderDto = null;
		List<Gender> genderType = null;

		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			genderType = genderTypeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.GENDER_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (!(genderType.isEmpty())) {
			genderDto = MapperUtils.mapAll(genderType, GenderDto.class);
		}
		return CompletableFuture.completedFuture(genderDto);
	}

	/**
	 * Method to fetch devices
	 * 
	 * @param regCenterId      registration center id
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link DeviceDto}
	 */
	@Async
	public CompletableFuture<List<DeviceDto>> getDevices(String regCenterId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<Device> devices = null;
		List<DeviceDto> deviceList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			devices = deviceRepository.findLatestDevicesByRegCenterId(regCenterId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICES_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (devices != null && !devices.isEmpty())
			deviceList = MapperUtils.mapAll(devices, DeviceDto.class);
		return CompletableFuture.completedFuture(deviceList);
	}

	/**
	 * Method to fetch document category
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link DocumentCategoryDto}
	 */
	@Async
	public CompletableFuture<List<DocumentCategoryDto>> getDocumentCategories(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<DocumentCategoryDto> documentCategoryList = null;
		List<DocumentCategory> documentCategories = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			documentCategories = documentCategoryRepository.findAllLatestCreatedUpdateDeleted(lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (documentCategories != null && !documentCategories.isEmpty())
			documentCategoryList = MapperUtils.mapAll(documentCategories, DocumentCategoryDto.class);

		return CompletableFuture.completedFuture(documentCategoryList);
	}

	/**
	 * Method to fetch document type
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link DocumentTypeDto}
	 */
	@Async
	public CompletableFuture<List<DocumentTypeDto>> getDocumentTypes(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<DocumentTypeDto> documentTypeList = null;
		List<DocumentType> documentTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			documentTypes = documentTypeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DOCUMENT_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (documentTypes != null && !documentTypes.isEmpty())
			documentTypeList = MapperUtils.mapAll(documentTypes, DocumentTypeDto.class);

		return CompletableFuture.completedFuture(documentTypeList);
	}

	/**
	 * Method to fetch id types
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link IdTypeDto}
	 */
	@Async
	public CompletableFuture<List<IdTypeDto>> getIdTypes(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<IdTypeDto> idTypeList = null;
		List<IdType> idTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			idTypes = idTypeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.ID_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (idTypes != null && !idTypes.isEmpty())
			idTypeList = MapperUtils.mapAll(idTypes, IdTypeDto.class);
		return CompletableFuture.completedFuture(idTypeList);
	}

	/**
	 * Method to fetch device specification
	 * 
	 * @param regCenterId      registration center id
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link DeviceSpecificationDto}}
	 */
	@Async
	public CompletableFuture<List<DeviceSpecificationDto>> getDeviceSpecifications(String regCenterId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<DeviceSpecification> deviceSpecificationList = null;
		List<DeviceSpecificationDto> deviceSpecificationDtoList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			deviceSpecificationList = deviceSpecificationRepository.findLatestDeviceTypeByRegCenterId(regCenterId,
					lastUpdated, currentTimeStamp);
		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICE_SPECIFICATION_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (deviceSpecificationList != null && !deviceSpecificationList.isEmpty())
			deviceSpecificationDtoList = MapperUtils.mapAll(deviceSpecificationList, DeviceSpecificationDto.class);
		return CompletableFuture.completedFuture(deviceSpecificationDtoList);

	}

	/**
	 * Method to fetch locations
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link LocationDto}
	 */
	@Async
	public CompletableFuture<List<LocationDto>> getLocationHierarchy(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<LocationDto> responseList = null;
		List<Location> locations = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}

			locations = locationRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (!locations.isEmpty()) {
			responseList = MapperUtils.mapAll(locations, LocationDto.class);
		}
		return CompletableFuture.completedFuture(responseList);
	}

	/**
	 * Method to fetch template types
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link TemplateTypeDto}
	 */
	@Async
	public CompletableFuture<List<TemplateTypeDto>> getTemplateTypes(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<TemplateTypeDto> templateTypeList = null;
		List<TemplateType> templateTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			templateTypes = templateTypeRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (templateTypes != null && !templateTypes.isEmpty())
			templateTypeList = MapperUtils.mapAll(templateTypes, TemplateTypeDto.class);

		return CompletableFuture.completedFuture(templateTypeList);
	}

	/**
	 * Gets the device type.
	 *
	 * @param regCenterId      the reg center id
	 * @param lastUpdated      the last updated
	 * @param currentTimeStamp the current time stamp
	 * @return {@link DeviceTypeDto}
	 */
	@Async
	public CompletableFuture<List<DeviceTypeDto>> getDeviceType(String regCenterId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<DeviceTypeDto> deviceTypeList = null;
		List<DeviceType> deviceTypes = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			deviceTypes = deviceTypeRepository.findLatestDeviceTypeByRegCenterId(regCenterId, lastUpdated,
					currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (deviceTypes != null && !deviceTypes.isEmpty()) {
			deviceTypeList = MapperUtils.mapAll(deviceTypes, DeviceTypeDto.class);
		}
		return CompletableFuture.completedFuture(deviceTypeList);
	}

	/**
	 * Method to fetch document mapping
	 * 
	 * @param lastUpdated      lastUpdated timestamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link ValidDocumentDto}
	 */
	@Async
	public CompletableFuture<List<ValidDocumentDto>> getValidDocuments(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp) {
		List<ValidDocumentDto> validDocumentList = null;
		List<ValidDocument> validDocuments = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			validDocuments = validDocumentRepository.findAllLatestCreatedUpdateDeleted(lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

		if (validDocuments != null && !validDocuments.isEmpty()) {
			validDocumentList = MapperUtils.mapAll(validDocuments, ValidDocumentDto.class);
		}
		return CompletableFuture.completedFuture(validDocumentList);
	}

	/**
	 * 
	 * @param regCenterId
	 * @param lastUpdated
	 * @param currentTimeStamp
	 * @return list of {@link RegistrationCenterMachineDto}
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterMachineDto>> getRegistrationCenterMachines(String regCenterId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterMachineDto> registrationCenterMachineDtos = new ArrayList<>();
		List<Machine> machines = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machines = machineRepository
					.findAllLatestCreatedUpdatedDeleted(regCenterId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (machines != null && !machines.isEmpty()) {
			for(Machine machine : machines) {
				
				RegistrationCenterMachineDto dto=new RegistrationCenterMachineDto();
				dto.setIsActive(machine.getIsActive());
				dto.setIsDeleted(machine.getIsDeleted());
				dto.setLangCode(machine.getLangCode());
				dto.setMachineId(machine.getId());
				dto.setRegCenterId(machine.getRegCenterId());
				registrationCenterMachineDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterMachineDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time stamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterDeviceDto}
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterDeviceDto>> getRegistrationCenterDevices(String regId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterDeviceDto> registrationCenterDeviceDtos = new ArrayList<>();
		List<Device> devices = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			devices = deviceRepository
					.findAllLatestByRegistrationCenterCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_DEVICE_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (devices != null && !devices.isEmpty()) {
			for(Device device : devices) {
				
				RegistrationCenterDeviceDto dto=new RegistrationCenterDeviceDto();
				dto.setIsActive(device.getIsActive());
				dto.setIsDeleted(device.getIsDeleted());
				dto.setLangCode(device.getLangCode());
				dto.setDeviceId(device.getId());
				dto.setRegCenterId(device.getRegCenterId());
				registrationCenterDeviceDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterDeviceDtos);
	}

	/**
	 * 
	 * @param regId            -registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterMachineDeviceDto} - list of
	 *         registration center machine device dto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterMachineDeviceDto>> getRegistrationCenterMachineDevices(String regId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterMachineDeviceDto> registrationCenterMachineDeviceDtos = new ArrayList<>();
		List<Machine> machines = null;
		List<Device> devices = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machines = machineRepository
					.findAllLatestCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);
			devices = deviceRepository
					.findAllLatestByRegistrationCenterCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_MACHINE_DEVICE_FETCH_EXCEPTION.getErrorCode(), e.getMessage(), e);
		}
		if (machines != null && !machines.isEmpty()) {
			if (devices != null && !devices.isEmpty()) {
				for(Device device : devices) {
					for(Machine machine : machines) {
						if(device.getLangCode().equals(machine.getLangCode())) {
							RegistrationCenterMachineDeviceDto dto=new RegistrationCenterMachineDeviceDto();
							if(device.getIsActive() == null&& machine.getIsActive()!= null)dto.setIsActive(machine.getIsActive());
							if(machine.getIsActive() == null&& device.getIsActive()!= null)dto.setIsActive(device.getIsActive());
							if(device.getIsActive() != null && machine.getIsActive()!= null) {
								dto.setIsActive(device.getIsActive() && machine.getIsActive());
							}
							if(device.getIsDeleted() == null&& machine.getIsDeleted()!= null)dto.setIsDeleted(machine.getIsDeleted());
							if(machine.getIsDeleted() == null&& device.getIsDeleted()!= null)dto.setIsDeleted(device.getIsDeleted());
							if(device.getIsDeleted() != null && machine.getIsDeleted()!= null) {
								dto.setIsDeleted(device.getIsDeleted() && machine.getIsDeleted());
							}
							
							dto.setLangCode(device.getLangCode());
							dto.setDeviceId(device.getId());
							dto.setMachineId(machine.getId());
							dto.setRegCenterId(device.getRegCenterId());
							registrationCenterMachineDeviceDtos.add(dto);
						}
					}
				}
			
			}
		}
		return CompletableFuture.completedFuture(registrationCenterMachineDeviceDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterUserMachineMappingDto} - list of
	 *         RegistrationCenterUserMachineMappingDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterUserMachineMappingDto>> getRegistrationCenterUserMachines(
			String regId, LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterUserMachineMappingDto> registrationCenterUserMachineMappingDtos = new ArrayList<>();
		List<Machine> machines = null;
		List<UserDetails> userDetails = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machines = machineRepository
					.findAllLatestCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);
			userDetails=userDetailsRepository.findAllLatestCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {
			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_USER_MACHINE_DEVICE_FETCH_EXCEPTION.getErrorCode(), e.getMessage(),
					e);
		}
		if (machines != null && !machines.isEmpty()) {
			if (userDetails != null && !userDetails.isEmpty()) {
				for(UserDetails userDetail : userDetails) {
					for(Machine machine : machines) {
						if(userDetail.getLangCode().equals(machine.getLangCode())) {
							RegistrationCenterUserMachineMappingDto dto=new RegistrationCenterUserMachineMappingDto();
							if(userDetail.getIsActive() == null&& machine.getIsActive()!= null)dto.setIsActive(machine.getIsActive());
							if(machine.getIsActive() == null&& userDetail.getIsActive()!= null)dto.setIsActive(userDetail.getIsActive());
							if(userDetail.getIsActive() != null && machine.getIsActive()!= null) {
								dto.setIsActive(userDetail.getIsActive() && machine.getIsActive());
							}
							if(userDetail.getIsDeleted() == null&& machine.getIsDeleted()!= null)dto.setIsDeleted(machine.getIsDeleted());
							if(machine.getIsDeleted() == null&& userDetail.getIsDeleted()!= null)dto.setIsDeleted(userDetail.getIsDeleted());
							if(userDetail.getIsDeleted() != null && machine.getIsDeleted()!= null) {
								dto.setIsDeleted(userDetail.getIsDeleted() && machine.getIsDeleted());
							}
							dto.setLangCode(userDetail.getLangCode());
							dto.setUsrId(userDetail.getId());
							dto.setMachineId(machine.getId());
							dto.setCntrId(userDetail.getRegCenterId());
							registrationCenterUserMachineMappingDtos.add(dto);
						}
					}
				}
			
			}
		}
		return CompletableFuture.completedFuture(registrationCenterUserMachineMappingDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterUserDto} - list of
	 *         RegistrationCenterUserDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterUserDto>> getRegistrationCenterUsers(String regId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterUserDto> registrationCenterUserDtos = new ArrayList<>();
		List<UserDetails> userDetails = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			userDetails=userDetailsRepository.findAllLatestCreatedUpdatedDeleted(regId, lastUpdated, currentTimeStamp);


		} catch (DataAccessException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_USER_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		if (userDetails != null && !userDetails.isEmpty()) {
			for(UserDetails userDetail : userDetails) {
				
				RegistrationCenterUserDto dto=new RegistrationCenterUserDto();
				dto.setIsActive(userDetail.getIsActive());
				dto.setIsDeleted(userDetail.getIsDeleted());
				dto.setLangCode(userDetail.getLangCode());
				dto.setUserId(userDetail.getId());
				dto.setRegCenterId(userDetail.getRegCenterId());
				registrationCenterUserDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterUserDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterUserHistoryDto} - list of
	 *         RegistrationCenterUserHistoryDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterUserHistoryDto>> getRegistrationCenterUserHistory(String regId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterUserHistoryDto> registrationCenterUserHistoryDtos = new ArrayList<>();
		List<UserDetailsHistory> userHistoryList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			userHistoryList = userDetailsHistoryRepository
					.findLatestRegistrationCenterUserHistory(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {

			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_USER_HISTORY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_USER_HISTORY_FETCH_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage(),
					e);
		}
		if (userHistoryList != null && !userHistoryList.isEmpty()) {
			for(UserDetailsHistory userDetail : userHistoryList) {
				
				RegistrationCenterUserHistoryDto dto=new RegistrationCenterUserHistoryDto();
				dto.setIsActive(userDetail.getIsActive());
				dto.setIsDeleted(userDetail.getIsDeleted());
				dto.setLangCode(userDetail.getLangCode());
				dto.setUserId(userDetail.getId());
				dto.setRegCntrId(userDetail.getRegCenterId());
				dto.setEffectDateTimes(userDetail.getEffDTimes());
				registrationCenterUserHistoryDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterUserHistoryDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterUserMachineMappingHistoryDto} - list
	 *         of RegistrationCenterUserMachineMappingHistoryDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterUserMachineMappingHistoryDto>> getRegistrationCenterUserMachineMapping(
			String regId, LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterUserMachineMappingHistoryDto> registrationCenterUserMachineMappingHistoryDtos = new ArrayList<>();
		List<MachineHistory> machines = null;
		List<UserDetailsHistory> userDetails = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machines = machineHistoryRepository
					.findLatestRegistrationCenterMachineHistory(regId, lastUpdated, currentTimeStamp);
			userDetails = userDetailsHistoryRepository
					.findLatestRegistrationCenterUserHistory(regId, lastUpdated, currentTimeStamp);
			

		} catch (DataAccessException e) {

			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_MACHINE_USER_HISTORY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_MACHINE_USER_HISTORY_FETCH_EXCEPTION.getErrorCode() + " "
							+ e.getMessage(),
					e);
		}
		if (machines != null && !machines.isEmpty()) {
			if (userDetails != null && !userDetails.isEmpty()) {
				for(UserDetailsHistory userDetail : userDetails) {
					for(MachineHistory machine : machines) {
						if(userDetail.getLangCode().equals(machine.getLangCode())) {
							RegistrationCenterUserMachineMappingHistoryDto dto=new RegistrationCenterUserMachineMappingHistoryDto();
							dto.setCntrId(userDetail.getRegCenterId());
							dto.setMachineId(machine.getId());
							dto.setUsrId(userDetail.getId());
							if(machine.getEffectDateTime()!=null &&userDetail.getEffDTimes() !=null) {
							dto.setEffectivetimes(machine.getEffectDateTime().isAfter( userDetail.getEffDTimes())? machine.getEffectDateTime() : userDetail.getEffDTimes() );
							}
							registrationCenterUserMachineMappingHistoryDtos.add(dto);
						}
					}
				}
			
			}
		}
		return CompletableFuture.completedFuture(registrationCenterUserMachineMappingHistoryDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterMachineDeviceHistoryDto} - list of
	 *         RegistrationCenterMachineDeviceHistoryDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterMachineDeviceHistoryDto>> getRegistrationCenterMachineDeviceHistoryDetails(
			String regId, LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterMachineDeviceHistoryDto> registrationCenterMachineDeviceHistoryDtos = new ArrayList<>();
		List<DeviceHistory> deviceHistoryList = null;
		List<MachineHistory> machines = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machines = machineHistoryRepository
					.findLatestRegistrationCenterMachineHistory(regId, lastUpdated, currentTimeStamp);
			deviceHistoryList = deviceHistoryRepository
					.findLatestRegistrationCenterDeviceHistory(regId, lastUpdated, currentTimeStamp);
		} catch (DataAccessException e) {

			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_MACHINE_DEVICE_HISTORY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_MACHINE_DEVICE_HISTORY_FETCH_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage(),
					e);
		}
		if (machines != null && !machines.isEmpty()) {
			if (deviceHistoryList != null && !deviceHistoryList.isEmpty()) {
				for(DeviceHistory device : deviceHistoryList) {
					for(MachineHistory machine : machines) {
						if(device.getLangCode().equals(device.getLangCode())) {
							RegistrationCenterMachineDeviceHistoryDto dto=new RegistrationCenterMachineDeviceHistoryDto();
							dto.setRegCenterId(device.getRegCenterId());
							dto.setMachineId(machine.getId());
							dto.setDeviceId(device.getId());
							if(device.getIsActive() == null)dto.setIsActive(machine.getIsActive());
							if(machine.getIsActive() == null)dto.setIsActive(device.getIsActive());
							if(device.getIsActive() != null && machine.getIsActive()!= null) {
								dto.setIsActive(device.getIsActive() && machine.getIsActive());
							}
							if(device.getIsDeleted() == null)dto.setIsDeleted(machine.getIsDeleted());
							if(machine.getIsDeleted() == null)dto.setIsDeleted(device.getIsDeleted());
							if(device.getIsDeleted() != null && machine.getIsDeleted()!= null) {
								dto.setIsDeleted(device.getIsDeleted() && machine.getIsDeleted());
							}
							if(device.getEffectDateTime() == null)dto.setEffectivetimes(machine.getEffectDateTime());
							if(machine.getEffectDateTime() == null)dto.setEffectivetimes(device.getEffectDateTime());
							if(device.getEffectDateTime() != null && machine.getEffectDateTime()!= null) {
								dto.setEffectivetimes(machine.getEffectDateTime().isAfter( device.getEffectDateTime())? machine.getEffectDateTime() : device.getEffectDateTime() );
							}
							dto.setLangCode(device.getLangCode());
							
							registrationCenterMachineDeviceHistoryDtos.add(dto);
						}
					}
				}
			
			}
		}
		return CompletableFuture.completedFuture(registrationCenterMachineDeviceHistoryDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterDeviceHistoryDto} - list of
	 *         RegistrationCenterDeviceHistoryDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterDeviceHistoryDto>> getRegistrationCenterDeviceHistoryDetails(
			String regId, LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterDeviceHistoryDto> registrationCenterDeviceHistoryDtos = new ArrayList<>();
		List<DeviceHistory> deviceHistoryList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			deviceHistoryList = deviceHistoryRepository
					.findLatestRegistrationCenterDeviceHistory(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {

			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_DEVICE_HISTORY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_DEVICE_HISTORY_FETCH_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage(),
					e);
		}
		if (deviceHistoryList != null && !deviceHistoryList.isEmpty()) {
			for(DeviceHistory device : deviceHistoryList) {
				
				RegistrationCenterDeviceHistoryDto dto=new RegistrationCenterDeviceHistoryDto();
				dto.setIsActive(device.getIsActive());
				dto.setIsDeleted(device.getIsDeleted());
				dto.setLangCode(device.getLangCode());
				dto.setDeviceId(device.getId());
				dto.setRegCenterId(device.getRegCenterId());
				dto.setEffectivetimes(device.getEffectDateTime());
				registrationCenterDeviceHistoryDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterDeviceHistoryDtos);
	}

	/**
	 * 
	 * @param regId            - registration center id
	 * @param lastUpdated      - last updated time
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link RegistrationCenterMachineHistoryDto} - list of
	 *         RegistrationCenterMachineHistoryDto
	 */
	@Async
	public CompletableFuture<List<RegistrationCenterMachineHistoryDto>> getRegistrationCenterMachineHistoryDetails(
			String regId, LocalDateTime lastUpdated, LocalDateTime currentTimeStamp) {
		List<RegistrationCenterMachineHistoryDto> registrationCenterMachineHistoryDtos = new ArrayList<>();
		List<MachineHistory> machineHistoryList = null;
		try {
			if (lastUpdated == null) {
				lastUpdated = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			machineHistoryList = machineHistoryRepository
					.findLatestRegistrationCenterMachineHistory(regId, lastUpdated, currentTimeStamp);

		} catch (DataAccessException e) {

			throw new SyncDataServiceException(
					MasterDataErrorCode.REG_CENTER_MACHINE_HISTORY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_MACHINE_HISTORY_FETCH_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage(),
					e);
		}
		if (machineHistoryList != null && !machineHistoryList.isEmpty()) {
			for(MachineHistory machineHistory : machineHistoryList) {
				
				RegistrationCenterMachineHistoryDto dto=new RegistrationCenterMachineHistoryDto();
				dto.setIsActive(machineHistory.getIsActive());
				dto.setIsDeleted(machineHistory.getIsDeleted());
				dto.setLangCode(machineHistory.getLangCode());
				dto.setMachineId(machineHistory.getId());
				dto.setRegCenterId(machineHistory.getRegCenterId());
				dto.setEffectivetimes(machineHistory.getEffectDateTime());
				registrationCenterMachineHistoryDtos.add(dto);
				
			}
			
		}
		return CompletableFuture.completedFuture(registrationCenterMachineHistoryDtos);
	}

	/**
	 * 
	 * @param lastUpdatedTime  - last updated time stamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link ApplicantValidDocumentDto}
	 */
	@Async
	public CompletableFuture<List<ApplicantValidDocumentDto>> getApplicantValidDocument(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<ApplicantValidDocumentDto> applicantValidDocumentDtos = null;
		List<ApplicantValidDocument> applicantValidDocuments = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			applicantValidDocuments = applicantValidDocumentRepository.findAllByTimeStamp(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(
					MasterDataErrorCode.APPLICANT_VALID_DOCUMENT_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.APPLICANT_VALID_DOCUMENT_FETCH_EXCEPTION.getErrorMessage());
		}
		if (applicantValidDocuments != null && !applicantValidDocuments.isEmpty()) {
			applicantValidDocumentDtos = MapperUtils.mapAll(applicantValidDocuments, ApplicantValidDocumentDto.class);
		}
		return CompletableFuture.completedFuture(applicantValidDocumentDtos);
	}

	/**
	 * 
	 * @param lastUpdatedTime  - last updated time stamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link IndividualTypeDto}
	 */
	@Async
	public CompletableFuture<List<IndividualTypeDto>> getIndividualType(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<IndividualType> individualTypes = null;
		List<IndividualTypeDto> individualTypeDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			individualTypes = individualTypeRepository.findAllIndvidualTypeByTimeStamp(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.INDIVIDUAL_TYPE_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.INDIVIDUAL_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		if (individualTypes != null && !individualTypes.isEmpty()) {
			individualTypeDtos = MapperUtils.mapAll(individualTypes, IndividualTypeDto.class);
		}
		return CompletableFuture.completedFuture(individualTypeDtos);

	}

	@Async
	public CompletableFuture<List<AppAuthenticationMethodDto>> getAppAuthenticationMethodDetails(
			LocalDateTime lastUpdatedTime, LocalDateTime currentTimeStamp) {
		List<AppAuthenticationMethod> appAuthenticationMethods = null;
		List<AppAuthenticationMethodDto> appAuthenticationMethodDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			appAuthenticationMethods = appAuthenticationMethodRepository
					.findByLastUpdatedAndCurrentTimeStamp(lastUpdatedTime, currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(
					MasterDataErrorCode.APP_AUTHORIZATION_METHOD_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.APP_AUTHORIZATION_METHOD_FETCH_EXCEPTION.getErrorMessage());
		}
		if (appAuthenticationMethods != null && !appAuthenticationMethods.isEmpty()) {
			appAuthenticationMethodDtos = MapperUtils.mapAll(appAuthenticationMethods,
					AppAuthenticationMethodDto.class);
		}
		return CompletableFuture.completedFuture(appAuthenticationMethodDtos);

	}

	@Async
	public CompletableFuture<List<AppDetailDto>> getAppDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<AppDetail> appDetails = null;
		List<AppDetailDto> appDetailDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			appDetails = appDetailRepository.findByLastUpdatedTimeAndCurrentTimeStamp(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.APP_DETAIL_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.APP_DETAIL_FETCH_EXCEPTION.getErrorMessage());
		}
		if (appDetails != null && !appDetails.isEmpty()) {
			appDetailDtos = MapperUtils.mapAll(appDetails, AppDetailDto.class);
		}
		return CompletableFuture.completedFuture(appDetailDtos);
	}

	@Async
	public CompletableFuture<List<AppRolePriorityDto>> getAppRolePriorityDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<AppRolePriority> appRolePriorities = null;
		List<AppRolePriorityDto> appRolePriorityDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			appRolePriorities = appRolePriorityRepository.findByLastUpdatedAndCurrentTimeStamp(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.APP_ROLE_PRIORITY_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.APP_ROLE_PRIORITY_FETCH_EXCEPTION.getErrorMessage());
		}
		if (appRolePriorities != null && !appRolePriorities.isEmpty()) {
			appRolePriorityDtos = MapperUtils.mapAll(appRolePriorities, AppRolePriorityDto.class);
		}
		return CompletableFuture.completedFuture(appRolePriorityDtos);
	}

	@Async
	public CompletableFuture<List<ScreenAuthorizationDto>> getScreenAuthorizationDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<ScreenAuthorization> screenAuthorizationList = null;
		List<ScreenAuthorizationDto> screenAuthorizationDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			screenAuthorizationList = screenAuthorizationRepository
					.findByLastUpdatedAndCurrentTimeStamp(lastUpdatedTime, currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.SCREEN_AUTHORIZATION_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.SCREEN_AUTHORIZATION_FETCH_EXCEPTION.getErrorMessage());
		}
		if (screenAuthorizationList != null && !screenAuthorizationList.isEmpty()) {
			screenAuthorizationDtos = MapperUtils.mapAll(screenAuthorizationList, ScreenAuthorizationDto.class);
		}
		return CompletableFuture.completedFuture(screenAuthorizationDtos);
	}

	@Async
	public CompletableFuture<List<ProcessListDto>> getProcessList(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<ProcessList> processList = null;
		List<ProcessListDto> processListDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			processList = processListRepository.findByLastUpdatedTimeAndCurrentTimeStamp(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.PROCESS_LIST_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.PROCESS_LIST_FETCH_EXCEPTION.getErrorMessage());
		}
		if (processList != null && !processList.isEmpty()) {
			processListDtos = MapperUtils.mapAll(processList, ProcessListDto.class);
		}
		return CompletableFuture.completedFuture(processListDtos);
	}

	@Async
	public CompletableFuture<List<SyncJobDefDto>> getSyncJobDefDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {

		if (lastUpdatedTime == null) {
			lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
		}
		List<SyncJobDefDto> syncJobDefDtos = syncJobDefService.getSyncJobDefDetails(lastUpdatedTime, currentTimeStamp);
		return CompletableFuture.completedFuture(syncJobDefDtos);
	}

	@Async
	public CompletableFuture<List<ScreenDetailDto>> getScreenDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<ScreenDetail> screenDetails = null;
		List<ScreenDetailDto> screenDetailDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			screenDetails = screenDetailRepository.findByLastUpdatedAndCurrentTimeStamp(lastUpdatedTime,
					currentTimeStamp);

		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.SCREEN_DETAIL_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.SCREEN_DETAIL_FETCH_EXCEPTION.getErrorMessage());
		}
		if (screenDetails != null && !screenDetails.isEmpty()) {
			screenDetailDtos = MapperUtils.mapAll(screenDetails, ScreenDetailDto.class);
		}
		return CompletableFuture.completedFuture(screenDetailDtos);
	}


	@Async
	public CompletableFuture<List<FoundationalTrustProviderDto>> getFPDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<FoundationalTrustProvider> foundationalTrustProviders = null;
		List<FoundationalTrustProviderDto> foundationalTrustProviderDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			foundationalTrustProviders = foundationalTrustProviderRepository
					.findAllLatestCreatedUpdateDeleted(lastUpdatedTime, currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.SCREEN_DETAIL_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.SCREEN_DETAIL_FETCH_EXCEPTION.getErrorMessage());
		}
		if (foundationalTrustProviders != null && !foundationalTrustProviders.isEmpty()) {
			foundationalTrustProviderDtos = MapperUtils.mapAll(foundationalTrustProviders,
					FoundationalTrustProviderDto.class);
		}
		return CompletableFuture.completedFuture(foundationalTrustProviderDtos);
	}

	@Async
	public CompletableFuture<List<DeviceTypeDPMDto>> getDeviceTypeDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<DeviceTypeDPM> deviceTypeDPMs = null;
		List<DeviceTypeDPMDto> deviceTypeDPMDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			deviceTypeDPMs = deviceTypeDPMRepository.findAllLatestCreatedUpdateDeleted(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.DEVICE_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		if (deviceTypeDPMs != null && !deviceTypeDPMs.isEmpty()) {
			deviceTypeDPMDtos = MapperUtils.mapAll(deviceTypeDPMs, DeviceTypeDPMDto.class);
		}
		return CompletableFuture.completedFuture(deviceTypeDPMDtos);
	}

	@Async
	public CompletableFuture<List<DeviceSubTypeDPMDto>> getDeviceSubTypeDetails(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp) {
		List<DeviceSubTypeDPM> deviceSubTypeDPMs = null;
		List<DeviceSubTypeDPMDto> deviceSubTypeDPMDtos = null;
		try {
			if (lastUpdatedTime == null) {
				lastUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
			}
			deviceSubTypeDPMs = deviceSubTypeDPMRepository.findAllLatestCreatedUpdateDeleted(lastUpdatedTime,
					currentTimeStamp);
		} catch (DataAccessException ex) {
			throw new SyncDataServiceException(MasterDataErrorCode.DEVICE_SUB_TYPE_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.DEVICE_SUB_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		if (deviceSubTypeDPMs != null && !deviceSubTypeDPMs.isEmpty()) {
			deviceSubTypeDPMDtos = MapperUtils.mapAll(deviceSubTypeDPMs, DeviceSubTypeDPMDto.class);
		}
		return CompletableFuture.completedFuture(deviceSubTypeDPMDtos);
	}

	public void getSyncDataBaseDto(Class entityClass, String entityType, List entities, String publicKey, List result) {
		getSyncDataBaseDto(entityClass.getSimpleName(), entityType, entities, publicKey, result);
	}

	@SuppressWarnings("unchecked")
	public void getSyncDataBaseDto(String entityName, String entityType, List entities, String publicKey, List result) {
		if(null != entities) {
			List<String> list = Collections.synchronizedList(new ArrayList<String>());
			entities.parallelStream().filter(Objects::nonNull).forEach(obj -> {
				try {
					String json = mapper.getObjectAsJsonString(obj);
					if(json != null) {
						list.add(json);
					}
				} catch (Exception e) {
					logger.error("Failed to map "+ entityName +" data to json", e);
				}
			});

			try {
				if(list.size() > 0) {
					TpmCryptoRequestDto tpmCryptoRequestDto = new TpmCryptoRequestDto();
					tpmCryptoRequestDto.setValue(CryptoUtil.encodeBase64(mapper.getObjectAsJsonString(list).getBytes()));
					tpmCryptoRequestDto.setPublicKey(publicKey);
					tpmCryptoRequestDto.setTpm(this.isTPMRequired);
					TpmCryptoResponseDto tpmCryptoResponseDto = clientCryptoManagerService.csEncrypt(tpmCryptoRequestDto);
					result.add(new SyncDataBaseDto(entityName, entityType, tpmCryptoResponseDto.getValue()));
				}
			} catch (Exception e) {
				logger.error("Failed to encrypt "+ entityName +" data to json", e);
			}
		}
	}

	/**
	 * This method queries registrationCenterMachineRepository to fetch active registrationCenterMachine
	 * with input keyIndex.
	 *
	 * KeyIndex is mandatory param
	 * registrationCenterId is optional, if provided validates, if this matches the mapped registration center
	 *
	 * @param registrationCenterId
	 * @param keyIndex
	 * @return RegistrationCenterMachineDto(machineId , registrationCenterId)
	 * @throws SyncDataServiceException
	 */
	public RegistrationCenterMachineDto getRegistrationCenterMachine(String registrationCenterId, String keyIndex) throws SyncDataServiceException {
		try {

			List<Object[]> regCenterMachines = machineRepository.getRegistrationCenterMachineWithKeyIndexWithoutStatusCheck(keyIndex);

			if (regCenterMachines.isEmpty()) {
				throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());
			}

			String mappedRegCenterId = (String)((Object[])regCenterMachines.get(0))[0];

			if(mappedRegCenterId == null)
				throw new RequestException(MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());

			if(registrationCenterId != null &&  !mappedRegCenterId.equals(registrationCenterId))
				throw new RequestException(MasterDataErrorCode.REG_CENTER_UPDATED.getErrorCode(),
						MasterDataErrorCode.REG_CENTER_UPDATED.getErrorMessage());

			return new RegistrationCenterMachineDto(mappedRegCenterId, (String)((Object[])regCenterMachines.get(0))[1],
					 (String)((Object[])regCenterMachines.get(0))[2]);


		} catch (DataAccessException | DataAccessLayerException e) {
			logger.error("Failed to fetch registrationCenterMachine : ", e);
		}

		throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
				MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage());
	}

}
