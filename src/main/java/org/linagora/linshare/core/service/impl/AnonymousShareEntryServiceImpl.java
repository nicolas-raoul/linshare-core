package org.linagora.linshare.core.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.linagora.linshare.core.business.service.AnonymousShareEntryBusinessService;
import org.linagora.linshare.core.business.service.DocumentEntryBusinessService;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.AnonymousShareEntry;
import org.linagora.linshare.core.domain.entities.AnonymousUrl;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.MailContainer;
import org.linagora.linshare.core.domain.entities.MailContainerWithRecipient;
import org.linagora.linshare.core.domain.entities.ShareLogEntry;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.service.AnonymousShareEntryService;
import org.linagora.linshare.core.service.FunctionalityService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.MailContentBuildingService;
import org.linagora.linshare.core.service.NotifierService;
import org.linagora.linshare.core.service.ShareExpiryDateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymousShareEntryServiceImpl implements AnonymousShareEntryService {

	private final FunctionalityService functionalityService;
	
	private final AnonymousShareEntryBusinessService anonymousShareEntryBusinessService;
	
	private final ShareExpiryDateService shareExpiryDateService;
	
	private final LogEntryService logEntryService;
	
	private final NotifierService notifierService;
    
    private final MailContentBuildingService mailElementsFactory;
    
    private final DocumentEntryBusinessService documentEntryBusinessService;
    
    
    private static final Logger logger = LoggerFactory.getLogger(AnonymousShareEntryServiceImpl.class);
    
	public AnonymousShareEntryServiceImpl(FunctionalityService functionalityService, AnonymousShareEntryBusinessService anonymousShareEntryBusinessService,
			ShareExpiryDateService shareExpiryDateService, LogEntryService logEntryService, NotifierService notifierService, MailContentBuildingService mailElementsFactory,
			DocumentEntryBusinessService documentEntryBusinessService) {
		super();
		this.functionalityService = functionalityService;
		this.anonymousShareEntryBusinessService = anonymousShareEntryBusinessService;
		this.shareExpiryDateService = shareExpiryDateService;
		this.logEntryService = logEntryService;
		this.notifierService = notifierService;
		this.mailElementsFactory = mailElementsFactory;
		this.documentEntryBusinessService = documentEntryBusinessService;
	}

	
	@Override
	public AnonymousShareEntry findByUuid(User actor, String shareUuid) throws BusinessException {
		AnonymousShareEntry share = anonymousShareEntryBusinessService.findByUuid(shareUuid);
		if(share == null) {
			logger.error("Share not found : " + shareUuid);
			throw new BusinessException(BusinessErrorCode.SHARED_DOCUMENT_NOT_FOUND, "Share entry not found : " + shareUuid);
		}
		if(share.getEntryOwner().equals(actor)) {
			return share;
		} else {
			logger.error("Actor " + actor.getAccountReprentation() + " does not own the share : " + shareUuid);
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to get this share, it does not belong to you.");
		}
	}
	

	@Override
	public List<AnonymousShareEntry> createAnonymousShare(List<DocumentEntry> documentEntries, User sender, Contact recipient, Calendar expirationDate, Boolean passwordProtected, MailContainer mailContainer) throws BusinessException {
		
		if(functionalityService.isSauMadatory(sender.getDomain().getIdentifier())) {
			passwordProtected = true;
		} else if(!functionalityService.isSauAllowed(sender.getDomain().getIdentifier())) {
			// if it is not mandatory an not allowed, it must be forbidden
			passwordProtected = false;
		}
		
		if (expirationDate == null) {
			expirationDate = shareExpiryDateService.computeMinShareExpiryDateOfList(documentEntries, sender);
		}
		
		AnonymousUrl anonymousUrl = anonymousShareEntryBusinessService.createAnonymousShare(documentEntries, sender, recipient, expirationDate, passwordProtected);
		
		
		// logs
		for (DocumentEntry documentEntry : documentEntries) {
			ShareLogEntry logEntry = new ShareLogEntry(sender.getMail(), sender.getFirstName(), sender.getLastName(), sender.getDomainId(),
		        	LogAction.FILE_SHARE, "Anonymous sharing of a file", documentEntry.getName(), documentEntry.getDocument().getSize(), documentEntry.getType(),
		        	recipient.getMail(), null, null, null, expirationDate);
		    logEntryService.create(logEntry);
		}
		
		
		
		sendNotification(documentEntries, sender, recipient, mailContainer, anonymousUrl);
		
		
		List<AnonymousShareEntry> anonymousShareEntries = new ArrayList<AnonymousShareEntry>(anonymousUrl.getAnonymousShareEntries());
		return anonymousShareEntries;
	}


	private void sendNotification(List<DocumentEntry> documentEntries, User sender, Contact recipient, MailContainer mailContainer, AnonymousUrl anonymousUrl) throws BusinessException {
		List<String> documentNames = new ArrayList<String>();
		Boolean isOneDocEncrypted = false;
		for (DocumentEntry documentEntry : documentEntries) {
			documentNames.add(documentEntry.getName());
			if(documentEntry.getCiphered()) isOneDocEncrypted = true;
		}
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		mailContainerWithRecipient.add(mailElementsFactory.buildMailNewSharingWithRecipient(sender, mailContainer, recipient, documentNames, anonymousUrl, isOneDocEncrypted));
		
		notifierService.sendAllNotifications(mailContainerWithRecipient);
	}
	

	@Override
	public List<AnonymousShareEntry> createAnonymousShare(List<DocumentEntry> documentEntries, User sender, List<Contact> recipients, Calendar expirationDate, Boolean passwordProtected, MailContainer mailContainer) throws BusinessException {
		List<AnonymousShareEntry> anonymousShareEntries = new ArrayList<AnonymousShareEntry>();
		for (Contact contact : recipients) {
			anonymousShareEntries.addAll(createAnonymousShare(documentEntries, sender, contact, expirationDate, passwordProtected, mailContainer));
		}
		return anonymousShareEntries;
	}


	@Override
	public void deleteShare(String shareUuid, User actor, MailContainer mailContainer) throws BusinessException {
		AnonymousShareEntry shareEntry = findByUuid(actor, shareUuid);
		anonymousShareEntryBusinessService.deleteAnonymousShare(shareEntry);
		//TODO AnonymousShareEntry notification
	}


	@Override
	public void deleteShare(AnonymousShareEntry share, User actor, MailContainer mailContainer) throws BusinessException {
		anonymousShareEntryBusinessService.deleteAnonymousShare(share);
		//TODO AnonymousShareEntry mail notification
		//send a notification by mail to the owner
//		List<String> docNames = new ArrayList<String>();
//		docNames.add(share.getName());
//		notifierService.sendAllNotifications(mailElementsFactory.buildMailAnonymousDownload(actor, mailContainer, docs, email, recipient)
	}
	
	
	@Override
	public InputStream getAnonymousShareEntryStream(String shareUuid, MailContainer mailContainer) throws BusinessException {
		try {
			AnonymousShareEntry shareEntry = downloadAnonymousShareEntry(shareUuid);
			//send a notification by mail to the owner
			String email = shareEntry.getContact().getMail();
			User owner = (User)shareEntry.getEntryOwner();
			List<String> docNames = new ArrayList<String>();
			docNames.add(shareEntry.getName());
			notifierService.sendAllNotifications(mailElementsFactory.buildMailAnonymousDownloadWithOneRecipient(owner, mailContainer, docNames, email, owner));
			
			return documentEntryBusinessService.getDocumentStream(shareEntry.getDocumentEntry());
		} catch (BusinessException e) {
			logger.error("Can't find anonymous share : " + shareUuid + " : " + e.getMessage());
			throw e;
		}
	}

	
	private AnonymousShareEntry downloadAnonymousShareEntry(String shareUuid) throws BusinessException {
		AnonymousShareEntry shareEntry = anonymousShareEntryBusinessService.findByUuid(shareUuid);
		if(shareEntry == null) {
			logger.error("Share not found : " + shareUuid);
			throw new BusinessException(BusinessErrorCode.SHARED_DOCUMENT_NOT_FOUND, "Share entry not found : " + shareUuid);
		}
		
		DocumentEntry documentEntry = shareEntry.getDocumentEntry();
		
		String email = shareEntry.getContact().getMail();
		User owner = (User)shareEntry.getEntryOwner();
		ShareLogEntry logEntry = new ShareLogEntry(owner.getMail(), owner
				.getFirstName(), owner.getLastName(), owner.getDomainId(),
				LogAction.ANONYMOUS_SHARE_DOWNLOAD, "Anonymous download of a file", documentEntry
				.getName(), documentEntry.getSize(), documentEntry
				.getType(), email!=null?email:"" , "", "" , "",null);
		
		logEntryService.create(logEntry);
		return shareEntry;
	}

	
	@Override
	public InputStream getAnonymousShareEntryStream(String shareUuid) throws BusinessException {
		try {
			AnonymousShareEntry shareEntry = downloadAnonymousShareEntry(shareUuid);
			return documentEntryBusinessService.getDocumentStream(shareEntry.getDocumentEntry());
		} catch (BusinessException e) {
			logger.error("Can't find anonymous share : " + shareUuid + " : " + e.getMessage());
			throw e;
		}
	}


	@Override
	public void sendDocumentEntryUpdateNotification(Account actor, AnonymousShareEntry anonymousShareEntry, String friendlySize, String originalFileName, MailContainer mailContainer) {
		//TODO AnonymousShareEntry notification
//		mailContainerWithRecipient.add(mailElementsFactory.buildMailSharedDocUpdatedWithRecipient(user, mailContainer, user, contact.getMail(), doc, oldFileName, fileSizeTxt, sUrlDownload, urlparam));
//		notifierService.sendAllNotifications(mailContainerWithRecipient);
	}
}