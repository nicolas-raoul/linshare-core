package org.linagora.linshare.core.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import org.linagora.linshare.core.business.service.DocumentEntryBusinessService;
import org.linagora.linshare.core.domain.constants.LinShareConstants;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.AntivirusLogEntry;
import org.linagora.linshare.core.domain.entities.Document;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.FileLogEntry;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.LogEntry;
import org.linagora.linshare.core.domain.entities.MimeTypeStatus;
import org.linagora.linshare.core.domain.entities.StringValueFunctionality;
import org.linagora.linshare.core.domain.objects.SizeUnitValueFunctionality;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.exception.TechnicalErrorCode;
import org.linagora.linshare.core.exception.TechnicalException;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.DocumentEntryService;
import org.linagora.linshare.core.service.FunctionalityService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.MimeTypeService;
import org.linagora.linshare.core.service.VirusScannerService;
import org.linagora.linshare.core.utils.DocumentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentEntryServiceImpl implements DocumentEntryService {

	private static final Logger logger = LoggerFactory.getLogger(DocumentEntryServiceImpl.class);
	
	private final DocumentEntryBusinessService documentEntryBusinessService;
	private final LogEntryService logEntryService;
	private final AbstractDomainService abstractDomainService;
	private final FunctionalityService functionalityService;
	private final MimeTypeService mimeTypeService;
	private final AccountService accountService;
	private final VirusScannerService virusScannerService;
	
	
	
	public DocumentEntryServiceImpl(DocumentEntryBusinessService documentEntryBusinessService, LogEntryService logEntryService, AbstractDomainService abstractDomainService,
			FunctionalityService functionalityService, MimeTypeService mimeTypeService, AccountService accountService, VirusScannerService virusScannerService) {
		super();
		this.documentEntryBusinessService = documentEntryBusinessService;
		this.logEntryService = logEntryService;
		this.abstractDomainService = abstractDomainService;
		this.functionalityService = functionalityService;
		this.mimeTypeService = mimeTypeService;
		this.accountService = accountService;
		this.virusScannerService = virusScannerService;
	}


	@Override
	public DocumentEntry createDocumentEntry(Account actor, InputStream stream, Long size, String fileName) throws BusinessException {
		AbstractDomain domain = abstractDomainService.retrieveDomain(actor.getDomain().getIdentifier());
		
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		String mimeType = documentEntryBusinessService.getMimeType(bufStream);
		checkSpace(size, fileName, actor);
		
		DocumentUtils util = new DocumentUtils();
		File tempFile =  util.getFileFromBufferedInputStream(bufStream, fileName);
		
		// check if the file MimeType is allowed
		Functionality mimeFunctionality = functionalityService.getMimeTypeFunctionality(domain);
		if(mimeFunctionality.getActivationPolicy().getStatus()) {
			checkFileMimeType(fileName, mimeType, actor);
		}
		
		Functionality antivirusFunctionality = functionalityService.getAntivirusFunctionality(domain);
		if(antivirusFunctionality.getActivationPolicy().getStatus()) {
			checkVirus(fileName, actor, stream);
		}

		// want a timestamp on doc ?
		String timeStampingUrl = null;
		StringValueFunctionality timeStampingFunctionality = functionalityService.getTimeStampingFunctionality(domain);
		if(timeStampingFunctionality.getActivationPolicy().getStatus()) {
			 timeStampingUrl = timeStampingFunctionality.getValue();
		}
				
				
		Functionality enciphermentFunctionality = functionalityService.getEnciphermentFunctionality(domain);
		Boolean checkIfIsCiphered = enciphermentFunctionality.getActivationPolicy().getStatus();
		
		DocumentEntry docEntry = documentEntryBusinessService.createDocumentEntry(actor, tempFile, size, fileName, checkIfIsCiphered, timeStampingUrl, mimeType);
		actor.getEntries().add(docEntry);
		accountService.update(actor);
	
		FileLogEntry logEntry = new FileLogEntry(actor, LogAction.FILE_UPLOAD, "Creation of a file", docEntry.getName(), docEntry.getDocument().getSize(), docEntry.getDocument().getType());
		logEntryService.create(logEntry);
		
		addDocSizeToGlobalUsedQuota(docEntry.getDocument(), domain);

		tempFile.delete(); // remove the temporary file
		return docEntry;
	}
	
	@Override
	public DocumentEntry updateDocumentEntry(Account actor, String docEntryUuid, InputStream stream, Long size, String fileName) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		if (!documentEntry.getEntryOwner().equals(actor)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to update this document.");
		}
		
		AbstractDomain domain = abstractDomainService.retrieveDomain(actor.getDomain().getIdentifier());
		String logText = documentEntry.getName(); //old name of the doc
		long oldDocSize = documentEntry.getDocument().getSize();
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		String mimeType = documentEntryBusinessService.getMimeType(bufStream);
		checkSpace(size, fileName, actor);
		
		DocumentUtils util = new DocumentUtils();
		File tempFile =  util.getFileFromBufferedInputStream(bufStream, fileName);
		
		// check if the file MimeType is allowed
		Functionality mimeFunctionality = functionalityService.getMimeTypeFunctionality(domain);
		if(mimeFunctionality.getActivationPolicy().getStatus()) {
			checkFileMimeType(fileName, mimeType, actor);
		}
		
		Functionality antivirusFunctionality = functionalityService.getAntivirusFunctionality(domain);
		if(antivirusFunctionality.getActivationPolicy().getStatus()) {
			checkVirus(fileName, actor, stream);
		}

		// want a timestamp on doc ?
		String timeStampingUrl = null;
		StringValueFunctionality timeStampingFunctionality = functionalityService.getTimeStampingFunctionality(domain);
		if(timeStampingFunctionality.getActivationPolicy().getStatus()) {
			 timeStampingUrl = timeStampingFunctionality.getValue();
		}
		
		Functionality enciphermentFunctionality = functionalityService.getEnciphermentFunctionality(domain);
		Boolean checkIfIsCiphered = enciphermentFunctionality.getActivationPolicy().getStatus();
		
		
		
		documentEntryBusinessService.updateDocumentEntry(actor, documentEntry, tempFile, size, fileName, checkIfIsCiphered, timeStampingUrl, mimeType);
		
		//put new file name in log
		//if the file is updated/replaced with a new file (new file name)
		//put new file name in log
		if (!logText.equalsIgnoreCase(documentEntry.getName())) {
			logText = documentEntry.getName() + " [" +logText+"]";
		}	
		
		FileLogEntry logEntry = new FileLogEntry(actor, LogAction.FILE_UPDATE, "Update of a file", logText, documentEntry.getDocument().getSize(), documentEntry.getDocument().getType());
		logEntryService.create(logEntry);
		
		removeDocSizeFromGlobalUsedQuota(oldDocSize, domain);
		addDocSizeToGlobalUsedQuota(documentEntry.getDocument(), domain);

		tempFile.delete(); // remove the temporary file
		return documentEntry;
	}

	
	
	@Override
	public DocumentEntry duplicateDocumentEntry(Account actor, String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		// TODO : Check the current doc entry id is shared with the actor (if not, you should not have the right to duplicate it)
//		if (!documentEntry.getEntryOwner().equals(actor)) {
//			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to update this document.");
//		}
		
		AbstractDomain domain = abstractDomainService.retrieveDomain(actor.getDomain().getIdentifier());
		
		checkSpace(documentEntry.getDocument().getSize(), documentEntry.getName(), actor);

		// want a timestamp on doc ?
		String timeStampingUrl = null;
		StringValueFunctionality timeStampingFunctionality = functionalityService.getTimeStampingFunctionality(domain);
		if(timeStampingFunctionality.getActivationPolicy().getStatus()) {
			 timeStampingUrl = timeStampingFunctionality.getValue();
		}
		
		documentEntryBusinessService.duplicateDocumentEntry(documentEntry, actor, timeStampingUrl);
		
		FileLogEntry logEntry = new FileLogEntry(actor, LogAction.FILE_UPLOAD, "Creation of a file", documentEntry.getName(), documentEntry.getDocument().getSize(), documentEntry.getDocument().getType());
		logEntryService.create(logEntry);
		
		addDocSizeToGlobalUsedQuota(documentEntry.getDocument(), domain);

		return documentEntry;
	}
	
	
	
	@Override
	public void deleteInconsistentDocumentEntry(String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		Account owner = documentEntry.getEntryOwner();
		try {
			
			if (documentEntry.getShareEntries().size() > 0) {
				throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to delete this document. It still exists shares.");
			}
			
			AbstractDomain domain = abstractDomainService.retrieveDomain(owner.getDomain().getIdentifier());
			removeDocSizeFromGlobalUsedQuota(documentEntry.getDocument().getSize(), domain);
			
			FileLogEntry logEntry  = new FileLogEntry(owner, LogAction.FILE_INCONSISTENCY, "File removed because of inconsistence. Please contact your administrator.",  documentEntry.getName(), documentEntry.getDocument().getSize(), documentEntry.getDocument().getType());
			logEntryService.create(LogEntryService.WARN, logEntry);
			documentEntryBusinessService.deleteDocumentEntry(documentEntry);
		} catch (IllegalArgumentException e) {
			logger.error("Could not delete file " + documentEntry.getName() + " of user " + owner.getLsUid() + ", reason : ", e);
			throw new TechnicalException(TechnicalErrorCode.COULD_NOT_DELETE_DOCUMENT, "Could not delete document");
		}
	}

	

	@Override
	public void deleteExpiratedDocumentEntry(String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		Account owner = documentEntry.getEntryOwner();
		try {
			
			if (documentEntry.getShareEntries().size() > 0) {
				throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to delete this document. It still exists shares.");
			}
			
			AbstractDomain domain = abstractDomainService.retrieveDomain(owner.getDomain().getIdentifier());
			removeDocSizeFromGlobalUsedQuota(documentEntry.getDocument().getSize(), domain);
			
			FileLogEntry logEntry  = new FileLogEntry(owner, LogAction.FILE_EXPIRE, "Expiration of a file",  documentEntry.getName(), documentEntry.getDocument().getSize(), documentEntry.getDocument().getType());
			logEntryService.create(LogEntryService.INFO, logEntry);
			documentEntryBusinessService.deleteDocumentEntry(documentEntry);
		} catch (IllegalArgumentException e) {
			logger.error("Could not delete file " + documentEntry.getName() + " of user " + owner.getLsUid() + ", reason : ", e);
			throw new TechnicalException(TechnicalErrorCode.COULD_NOT_DELETE_DOCUMENT, "Could not delete document");
		}
	}


	@Override
	public void deleteDocumentEntry(Account actor, String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		try {
			if (!documentEntry.getEntryOwner().equals(actor)) {
				throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to delete this document.");
			}
			
			if (documentEntry.getShareEntries().size() > 0) {
				throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to delete this document. It still exists shares.");
			}
			
			AbstractDomain domain = abstractDomainService.retrieveDomain(actor.getDomain().getIdentifier());
			removeDocSizeFromGlobalUsedQuota(documentEntry.getDocument().getSize(), domain);
			
			
			FileLogEntry logEntry = new FileLogEntry(actor, LogAction.FILE_DELETE, "Deletion of a file", documentEntry.getName(), documentEntry.getDocument().getSize(), documentEntry.getDocument().getType());
			logEntryService.create(LogEntryService.INFO, logEntry);
			documentEntryBusinessService.deleteDocumentEntry(documentEntry);
			
		} catch (IllegalArgumentException e) {
			logger.error("Could not delete file " + documentEntry.getName()
					+ " of user " + actor.getLsUid() + ", reason : ", e);
			throw new TechnicalException(TechnicalErrorCode.COULD_NOT_DELETE_DOCUMENT, "Could not delete document");
		}
	}


	@Override
	public long getUserMaxFileSize(Account account) throws BusinessException {
		
		//if user is not in one domain = BOUM
		
		AbstractDomain domain = abstractDomainService.retrieveDomain(account.getDomain().getIdentifier());
		
		SizeUnitValueFunctionality userMaxFileSizeFunctionality = functionalityService.getUserMaxFileSizeFunctionality(domain);
		
		
		if(userMaxFileSizeFunctionality.getActivationPolicy().getStatus()) {
			
			long maxSize = userMaxFileSizeFunctionality.getPlainSize();					
			if (maxSize < 0) {
				maxSize = 0;
			}
			return maxSize;
		}
		return LinShareConstants.defaultMaxFileSize;
	}
	
	
	@Override
	public long getAvailableSize(Account account) throws BusinessException {
		
		//if user is not in one domain = BOUM
		
		AbstractDomain domain = abstractDomainService.retrieveDomain(account.getDomain().getIdentifier());
		
		SizeUnitValueFunctionality globalQuotaFunctionality = functionalityService.getGlobalQuotaFunctionality(domain);
		SizeUnitValueFunctionality userQuotaFunctionality = functionalityService.getUserQuotaFunctionality(domain);
		
		
		if(globalQuotaFunctionality.getActivationPolicy().getStatus()) {
		
			long availableSize = globalQuotaFunctionality.getPlainSize() - domain.getUsedSpace().longValue();					
			if (availableSize < 0) {
				availableSize = 0;
			}
			return availableSize;
			
		} else if(userQuotaFunctionality.getActivationPolicy().getStatus()) {
				
			// use config parameter
			long userQuota = userQuotaFunctionality.getPlainSize();
	
			// TODO : To be fix : quota
//			if ((user.getDocuments() == null) || (user.getDocuments().size() == 0)) {
//				return userQuota;
//			}
//	
//			for (Document userDoc : user.getDocuments()) {
//				userQuota -= userDoc.getSize();
//			}
			return userQuota;
		}
		return LinShareConstants.defaultFreeSpace;
	}

	
	@Override
	public long getTotalSize(Account account) throws BusinessException {
		
		AbstractDomain domain = abstractDomainService.retrieveDomain(account.getDomain().getIdentifier());
		
		SizeUnitValueFunctionality globalQuotaFunctionality = functionalityService.getGlobalQuotaFunctionality(domain);
		SizeUnitValueFunctionality userQuotaFunctionality = functionalityService.getUserQuotaFunctionality(domain);
		
		if(globalQuotaFunctionality.getActivationPolicy().getStatus()) {
			return globalQuotaFunctionality.getPlainSize();
		}
		
		long userQuota = userQuotaFunctionality.getPlainSize();

		return userQuota;

	}
	
	
	@Override
	public boolean documentHasThumbnail(Account owner, String docEntryUuid) {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		if(documentEntry == null) {
			logger.error("Can't find document entry, are you sure it is not a share ? : " + docEntryUuid);
		} else if (documentEntry.getEntryOwner().equals(owner)) {
			String thmbUUID = documentEntry.getDocument().getThmbUuid();
			return (thmbUUID!=null && thmbUUID.length()>0);
		}
		return false;
	}

	@Override
	public InputStream getDocumentThumbnailStream(Account owner, String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		if(documentEntry == null) {
			logger.error("Can't find document entry, are you sure it is not a share ? : " + docEntryUuid);
			return null;
		} else if (!documentEntry.getEntryOwner().equals(owner)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to get thumbnail for this document.");
		} else {
			return documentEntryBusinessService.getDocumentThumbnailStream(documentEntry);
		}
	}
	
	@Override
	public InputStream getDocumentStream(Account owner, String docEntryUuid) throws BusinessException {
		DocumentEntry documentEntry = documentEntryBusinessService.findById(docEntryUuid);
		if(documentEntry == null) {
			logger.error("Can't find document entry, are you sure it is not a share ? : " + docEntryUuid);
			return null;
		} else if (!documentEntry.getEntryOwner().equals(owner)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to get this document.");
		} else {
			return documentEntryBusinessService.getDocumentStream(documentEntry);
		}
	}


	@Override
	public boolean isSignatureActive(Account account) {
		return functionalityService.getSignatureFunctionality(account.getDomain()).getActivationPolicy().getStatus();
	}

	@Override
	public boolean isEnciphermentActive(Account account) {
		return functionalityService.getEnciphermentFunctionality(account.getDomain()).getActivationPolicy().getStatus();
	}

	@Override
	public boolean isGlobalQuotaActive(Account account) throws BusinessException {
		return functionalityService.getGlobalQuotaFunctionality(account.getDomain()).getActivationPolicy().getStatus();
	}

	@Override
	public boolean isUserQuotaActive(Account account) throws BusinessException {
		return functionalityService.getUserQuotaFunctionality(account.getDomain()).getActivationPolicy().getStatus();
	}

	@Override
	public Long getGlobalQuota(Account account) throws BusinessException {
		SizeUnitValueFunctionality globalQuotaFunctionality = functionalityService.getGlobalQuotaFunctionality(account.getDomain());
		return globalQuotaFunctionality.getPlainSize();
	}
	
	
	@Override
	public DocumentEntry findById(Account actor, String currentDocEntryUuid) throws BusinessException {
		DocumentEntry entry = documentEntryBusinessService.findById(currentDocEntryUuid);
		if (!entry.getEntryOwner().equals(actor)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to get this document.");
		}
		return entry;
	}


	@Override
	public void renameDocumentEntry(Account actor, String docEntryUuid, String newName) throws BusinessException {
		DocumentEntry entry = documentEntryBusinessService.findById(docEntryUuid);
		if (!entry.getEntryOwner().equals(actor)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to rename this document.");
		}
		documentEntryBusinessService.renameDocumentEntry(entry, newName);
	}

	
	@Override
	public void updateFileProperties(Account actor, String docEntryUuid, String newName, String fileComment) throws BusinessException {
		DocumentEntry entry = documentEntryBusinessService.findById(docEntryUuid);
		if (!entry.getEntryOwner().equals(actor)) {
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to rename this document.");
		}
		documentEntryBusinessService.updateFileProperties(entry, newName, fileComment);
	}
	

	
	
	


	private void checkSpace(long size, String fileName, Account owner) throws BusinessException {
		// check the user quota
		if (getAvailableSize(owner) < size) {
			logger.info("The file  " + fileName + " is too large to fit in " + owner.getLsUid() + " user's space");
            String[] extras = {fileName};
			throw new BusinessException(BusinessErrorCode.FILE_TOO_LARGE, "The file is too large to fit in user's space", extras);
		}
	}
	
	private Boolean checkVirus(String fileName, Account owner, InputStream stream) throws BusinessException {
		if (logger.isDebugEnabled()) {
			logger.debug("antivirus activation:" + !virusScannerService.isDisabled());
		}

		boolean checkStatus = false;
		try {
			checkStatus = virusScannerService.check(stream);
		} catch (TechnicalException e) {
			LogEntry logEntry = new AntivirusLogEntry(owner, LogAction.ANTIVIRUS_SCAN_FAILED, e.getMessage());
			logger.error("File scan failed: antivirus enabled but not available ?");
			logEntryService.create(LogEntryService.ERROR,logEntry);
			throw new BusinessException(BusinessErrorCode.FILE_SCAN_FAILED, "File scan failed", e);
		}
		// check if the file contains virus
		if (!checkStatus) {
			LogEntry logEntry = new AntivirusLogEntry(owner, LogAction.FILE_WITH_VIRUS, fileName);
			logEntryService.create(LogEntryService.WARN,logEntry);
			logger.warn(owner.getLsUid() + " tried to upload a file containing virus:" + fileName);
		    String[] extras = {fileName};
			throw new BusinessException(BusinessErrorCode.FILE_CONTAINS_VIRUS, "File contains virus", extras);
		}
		return checkStatus;
	}
	
	private void checkFileMimeType(String fileName, String mimeType, Account owner) throws BusinessException {
		// use mimetype filtering
		if (logger.isDebugEnabled()) {
			logger.debug("2)check the type mime:" + mimeType);
		}

		// if we refuse some type of mime type
		if (mimeType != null) {
			MimeTypeStatus status = mimeTypeService.giveStatus(mimeType);

			if (status==MimeTypeStatus.DENIED) {
				if (logger.isDebugEnabled())
					logger.debug("mimetype not allowed: " + mimeType);
                String[] extras = {fileName};
				throw new BusinessException(
						BusinessErrorCode.FILE_MIME_NOT_ALLOWED,
						"This kind of file is not allowed: " + mimeType, extras);
			} else if(status==MimeTypeStatus.WARN){
				if (logger.isInfoEnabled())
					logger.info("mimetype warning: " + mimeType + "for user: "+owner.getLsUid());
			}
		} else {
			//type mime is null ?
            String[] extras = {fileName};
			throw new BusinessException(BusinessErrorCode.FILE_MIME_NOT_ALLOWED,
                "type mime is empty for this file" + mimeType, extras);
		}
	}
	
	private void addDocSizeToGlobalUsedQuota(Document docEntity, AbstractDomain domain) throws BusinessException {
		long newUsedQuota = domain.getUsedSpace().longValue() + docEntity.getSize();
		domain.setUsedSpace(newUsedQuota);
		abstractDomainService.updateDomain(domain);
	}
	
	private void removeDocSizeFromGlobalUsedQuota(long docSize, AbstractDomain domain)  throws BusinessException {
		long newUsedQuota = domain.getUsedSpace().longValue() - docSize;
		domain.setUsedSpace(newUsedQuota);
		abstractDomainService.updateDomain(domain);
	}
	
}