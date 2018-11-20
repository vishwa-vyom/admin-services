package io.mosip.registration.processor.packet.archiver.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.core.builder.CoreAuditRequestBuilder;
import io.mosip.registration.processor.core.constant.EventId;
import io.mosip.registration.processor.core.constant.EventName;
import io.mosip.registration.processor.core.constant.EventType;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.FilesystemCephAdapterImpl;
import io.mosip.registration.processor.packet.archiver.util.exception.PacketNotFoundException;
import io.mosip.registration.processor.packet.archiver.util.exception.UnableToAccessPathException;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;

/**
 * The Class PacketArchiver.
 * 
 * @author M1039285
 */
@Component
public class PacketArchiver {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketArchiver.class);

	@Autowired
	FilesystemCephAdapterImpl filesystemCephAdapterImpl;

	/** The filemanager. */
	@Autowired
	protected FileManager<DirectoryPathDto, InputStream> filemanager;

    private static final String APPENDER = "_";

	/** The core audit request builder. */
	@Autowired
	CoreAuditRequestBuilder coreAuditRequestBuilder;

	/** The Constant LOGDISPLAY. */
	private static final String LOGDISPLAY = "{} - {} - {}";

	/** The is transaction successful. */
	private boolean isTransactionSuccessful = false;
	/**
	 * Archive packet.
	 *
	 * @param registrationId
	 *            the registration id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnableToAccessPathException
	 *             the unable to access path exception
	 * @throws PacketNotFoundException
	 *             the packet not found exception
	 */
	public void archivePacket(String registrationId)
			throws IOException, UnableToAccessPathException, PacketNotFoundException {
		String description = "";
		String eventId = "";
		String eventName = "";
		String eventType = "";
		InputStream encryptedpacket = filesystemCephAdapterImpl.getPacket(registrationId);
		registrationId = registrationId + APPENDER + DateUtils.formatDate(new Date(), "YYYYMMddhhmmss");

		try {
			if (encryptedpacket != null) {
				filemanager.put(registrationId, encryptedpacket, DirectoryPathDto.ARCHIVE_LOCATION);
				isTransactionSuccessful=true;
			} else {

				throw new PacketNotFoundException(PlatformErrorMessages.RPR_PDJ_PACKET_NOT_AVAILABLE.getMessage());
			}

		} catch (IOException e) {
			LOGGER.error(LOGDISPLAY,"Packet archive failed", e);
			throw new UnableToAccessPathException(PlatformErrorMessages.RPR_PDJ_FILE_PATH_NOT_ACCESSIBLE.getMessage());
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName=	eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString(): EventName.EXCEPTION.toString();
			eventType=	eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString(): EventType.SYSTEM.toString();
			description = isTransactionSuccessful ? "The file is successfully copied to VM for registration Id :"+registrationId : "The file copying to VM is failured for registration Id: "+registrationId;

			coreAuditRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,registrationId);
		}
	}

}
