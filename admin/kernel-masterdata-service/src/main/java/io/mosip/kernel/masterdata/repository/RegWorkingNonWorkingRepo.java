package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.dto.DayNameAndSeqListDto;
import io.mosip.kernel.masterdata.dto.getresponse.WeekDaysDto;
import io.mosip.kernel.masterdata.entity.RegWorkingNonWorking;
import io.mosip.kernel.masterdata.entity.id.RegWorkingNonWorkingId;

@Repository("workingDaysRepo")
public interface RegWorkingNonWorkingRepo extends BaseRepository<RegWorkingNonWorking, RegWorkingNonWorkingId> {

	@Query("SELECT new io.mosip.kernel.masterdata.dto.DayNameAndSeqListDto(d.name,d.daySeq) FROM DaysOfWeek d where d.code in"
			+ "(SELECT w.dayCode from RegWorkingNonWorking w where w.registrationCenterId=?1  and w.isWorking=true and"
			+ " (w.isDeleted is null or w.isDeleted = false) and w.isActive = true) and d.langCode=?2 and "
			+ "d.isActive = true and (d.isDeleted is null or d.isDeleted = false)")
	List<DayNameAndSeqListDto> findByregistrationCenterIdAndlanguagecodeForWorkingDays(String regCenterId,
			String langcode);

	@Query("SELECT new io.mosip.kernel.masterdata.dto.getresponse.WeekDaysDto(d.name,d.isGlobalWorking,w.dayCode,w.languagecode,w.isWorking) "
			+ "FROM RegWorkingNonWorking w RIGHT JOIN w.daysOfWeek d "
			+ "where w.registrationCenterId=?1 and w.languagecode=?2 and w.isActive = true")
	List<WeekDaysDto> findByregistrationCenterIdAndlangCodeForWeekDays(String regCenterId, String langCode);

	@Query("From RegWorkingNonWorking where registrationCenterId=?1 and languagecode=?2 and (isDeleted is null or isDeleted = false) and isActive = true")
	List<RegWorkingNonWorking> findByRegCenterIdAndlanguagecode(String registrationCenterId, String languagecode);

	@Query("From RegWorkingNonWorking where languagecode=?1 and (isDeleted is null or isDeleted = false) and isActive = true")
	List<RegWorkingNonWorking> findByLanguagecode(String languageCode);

}
