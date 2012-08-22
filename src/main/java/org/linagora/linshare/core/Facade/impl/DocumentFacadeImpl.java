/*
 *    This file is part of Linshare.
 *
 *   Linshare is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   Linshare is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public
 *   License along with Foobar.  If not, see
 *                                    <http://www.gnu.org/licenses/>.
 *
 *   (c) 2008 Groupe Linagora - http://linagora.org
 *
*/
package org.linagora.linshare.core.Facade.impl;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.linagora.linshare.core.Facade.DocumentFacade;
import org.linagora.linshare.core.domain.constants.EntryType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.Entry;
import org.linagora.linshare.core.domain.entities.MailContainer;
import org.linagora.linshare.core.domain.entities.Signature;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.transformers.impl.DocumentEntryTransformer;
import org.linagora.linshare.core.domain.transformers.impl.SignatureTransformer;
import org.linagora.linshare.core.domain.vo.DisplayableAccountOccupationEntryVo;
import org.linagora.linshare.core.domain.vo.DocumentVo;
import org.linagora.linshare.core.domain.vo.SignatureVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.AnonymousShareEntryService;
import org.linagora.linshare.core.service.DocumentEntryService;
import org.linagora.linshare.core.service.EnciphermentService;
import org.linagora.linshare.core.service.ShareEntryService;
import org.linagora.linshare.core.service.ShareService;
import org.linagora.linshare.core.service.SignatureService;
import org.linagora.linshare.view.tapestry.beans.AccountOccupationCriteriaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DocumentFacadeImpl implements DocumentFacade {

	private final UserRepository<User> userRepository;
	
	private final SignatureTransformer signatureTransformer;
	
	private final EnciphermentService enciphermentService;
	
	private final DocumentEntryService documentEntryService;
	
	private final ShareEntryService shareEntryService;
	
	private final ShareService shareService;
	
	private final AnonymousShareEntryService anonymousShareEntryService;
	
	private final AccountService accountService;
	
	private final DocumentEntryTransformer documentEntryTransformer;
	
	private final SignatureService signatureService;
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentFacadeImpl.class);
	
	
	
	
	public DocumentFacadeImpl(UserRepository<User> userRepository, SignatureTransformer signatureTransformer,
			EnciphermentService enciphermentService, DocumentEntryService documentEntryService, AccountService accountService,
			DocumentEntryTransformer documentEntryTransformer, ShareEntryService shareEntryService, SignatureService signatureService, AnonymousShareEntryService anonymousShareEntryService, 
			ShareService shareService) {
		super();
		this.userRepository = userRepository;
		this.signatureTransformer = signatureTransformer;
		this.enciphermentService = enciphermentService;
		this.documentEntryService = documentEntryService;
		this.shareEntryService = shareEntryService;
		this.accountService = accountService;
		this.documentEntryTransformer = documentEntryTransformer;
		this.signatureService = signatureService;
		this.anonymousShareEntryService = anonymousShareEntryService;
		this.shareService = shareService;
	}


	@Override
	public DocumentVo insertFile(InputStream file, long size, String fileName, UserVo owner) throws BusinessException {
		Account actor = accountService.findByLsUid(owner.getLsUid());
		DocumentEntry createDocumentEntry = documentEntryService.createDocumentEntry(actor, file, size, fileName);
		return documentEntryTransformer.disassemble(createDocumentEntry);
	}


	@Override
	public void removeDocument(UserVo actorVo, DocumentVo document, MailContainer mailContainer) throws BusinessException {
		User actor = (User) accountService.findByLsUid(actorVo.getLsUid());
		if(actor != null) {
			shareService.deleteAllShareEntriesWithDocumentEntry(document.getIdentifier(), actor, mailContainer);
		} else {
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND, "The user couldn't be found");
		}
	}

	
	@Override
	public DocumentVo getDocument(String login, String uuid) {
		Account actor = accountService.findByLsUid(login);
		if(actor != null) {
			DocumentEntry entry;
			try {
				entry = documentEntryService.findById(actor, uuid);
				return documentEntryTransformer.disassemble(entry);
			} catch (BusinessException e) {
				logger.error("can't get document : " + e.getMessage());
			}
		}
		return null;
	}
	
	
	@Override
	public InputStream retrieveFileStream(DocumentVo doc, String lsUid) throws BusinessException {
		Account actor = accountService.findByLsUid(lsUid);
		return documentEntryService.getDocumentStream(actor, doc.getIdentifier());
	}
	
	
	@Override
	public InputStream retrieveFileStream(DocumentVo doc, UserVo actorVo) throws BusinessException {
		return retrieveFileStream(doc, actorVo.getLsUid());
	}

	
	@Override
	public void insertSignatureFile(InputStream file, long size, String fileName, UserVo ownerVo, DocumentVo documentVo, X509Certificate signerCertificate) throws BusinessException {
		Account actor = accountService.findByLsUid(ownerVo.getLsUid());
		DocumentEntry documentEntry = documentEntryService.findById(actor, documentVo.getIdentifier());
		signatureService.createSignature(actor, documentEntry.getDocument(), file, size, fileName, signerCertificate);
	}
	
	
	@Override
	public List<SignatureVo> getAllSignatures(UserVo userVo, DocumentVo documentVo){
		Account actor = accountService.findByLsUid(userVo.getLsUid());
		try {
			DocumentEntry document = documentEntryService.findById(actor, documentVo.getIdentifier());
			return signatureTransformer.disassembleList(new ArrayList<Signature>(document.getDocument().getSignatures()));
		} catch (BusinessException e) {
			logger.error("Can't find document : " + documentVo.getIdentifier() + ": " + e.getMessage());
		}
		return null;
	}

	
	@Override
	public SignatureVo getSignature(UserVo currentSigner, DocumentVo documentVo) {
		Account actor = accountService.findByLsUid(currentSigner.getLsUid());
		try {
			DocumentEntry document = documentEntryService.findById(actor, documentVo.getIdentifier());
			SignatureVo res = null;
			for (Signature signature : document.getDocument().getSignatures()) {
				if (signature.getSigner().equals(actor)) {
					res = signatureTransformer.disassemble(signature);
					break;
				}
			}
			return res;
			
		} catch (BusinessException e) {
			logger.error("Can't find document : " + documentVo.getIdentifier() + ": " + e.getMessage());
		}
		return null;
	}
	
	
	
	@Override
	public boolean isSignedDocument(String userLsUuid, DocumentVo documentVo){
		// Fix me : just write a finder
		boolean res = false;
		Account actor = accountService.findByLsUid(userLsUuid);
		try {
			DocumentEntry document = documentEntryService.findById(actor, documentVo.getIdentifier());
			Set<Signature> signatures = document.getDocument().getSignatures();
			if(signatures!=null && signatures.size()>0) res = true;
		} catch (BusinessException e) {
			logger.error("Can't find document : " + documentVo.getIdentifier() + ": " + e.getMessage());
		}
		return res;
	}
	

	@Override
	public boolean isSignedDocumentByCurrentUser(UserVo currentSigner, DocumentVo documentVo){
		
//		TODO : Fix signature : just write a finder 
//		User currentSignerDoc =  userRepository.findByLsUid(currentSigner.getLogin());
//		
//		Document doc = documentService.getDocument(document.getIdentifier());
//		List<Signature> signatures = doc.getSignatures();
//		
//		boolean res = false;
//		
//		for (Signature signature : signatures) {
//			if (signature.getSigner().equals(currentSignerDoc)) {
//				res = true;
//				break;
//			}
//		}
//		
//		return res;
		return false;
	}
	
	
	
	@Override
	public InputStream retrieveSignatureFileStream(SignatureVo signaturedoc){
		Signature signature = signatureService.findByUuid(signaturedoc.getIdentifier());
		return signatureService.getDocumentStream(signature);
	}
	

	@Override
	public Long getUserAvailableQuota(UserVo userVo) throws BusinessException {
		Account account = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.getAvailableSize(account);
	}
	
	@Override
	public Long getUserMaxFileSize(UserVo userVo) throws BusinessException {
		Account account = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.getUserMaxFileSize(account);
	}
	
	
	@Override
	public List<DisplayableAccountOccupationEntryVo> getAccountOccupationStat(AccountOccupationCriteriaBean criteria) throws BusinessException {
		List<DisplayableAccountOccupationEntryVo> result = new ArrayList<DisplayableAccountOccupationEntryVo>();
		
		List<User> users = userRepository.findByCriteria(criteria);
		for (User user : users) {
			if (user.getDomain() !=null) {
				//user needs to be in a domain to compute quotas
				DisplayableAccountOccupationEntryVo accountOccupation = getAccountStats(user);
				result.add(accountOccupation);
			}
		}
		
		return result;
	}

	
	private DisplayableAccountOccupationEntryVo getAccountStats(User user) throws BusinessException {
		Long userAvailableQuota = documentEntryService.getAvailableSize(user);
		Long userTotalQuota = documentEntryService.getTotalSize(user);
		Long userUsedSize = 0L;
		for (Entry entry : user.getEntries()) {
			if(entry.getEntryType().equals(EntryType.DOCUMENT)) {
				userUsedSize += ((DocumentEntry)entry).getSize();
			}
		}
		DisplayableAccountOccupationEntryVo accountOccupation = new DisplayableAccountOccupationEntryVo(user.getFirstName(), 
				user.getLastName(), user.getMail(), user.getAccountType(), 
				userAvailableQuota, userTotalQuota, userUsedSize);
		return accountOccupation;
	}

	
	@Override
	public DocumentVo encryptDocument(DocumentVo doc,UserVo user, String password) throws BusinessException{
		// TODO : Fix enciphermentService
		return null;
//		Document docenc = enciphermentService.encryptDocument(doc, user, password);
//		return documentTransformer.disassemble(documentService.getDocument(docenc.getUuid()));
	}

	
	@Override
	public DocumentVo decryptDocument(DocumentVo doc, UserVo user,String password) throws BusinessException {
		// TODO : Fix enciphermentService
		return null;
//		Document docenc = enciphermentService.decryptDocument(doc, user, password);
//		return documentTransformer.disassemble(documentService.getDocument(docenc.getUuid()));
	}
	
	
	@Override
	public boolean isDocumentEncrypted(DocumentVo doc) {
		// TODO : Fix enciphermentService
		return false;
//		return enciphermentService.isDocumentEncrypted(doc);
	}

	
	@Override
	public Long getUserTotalQuota(UserVo userVo) throws BusinessException {
		Account account = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.getTotalSize(account);
	}
	
	@Override
	public DocumentVo updateDocumentContent(String currentFileUUID, InputStream file, long size, String fileName, UserVo ownerVo, MailContainer mailContainer, String friendlySize) throws BusinessException {
		Account actor = accountService.findByLsUid(ownerVo.getLsUid());
		
		DocumentEntry originalEntry = documentEntryService.findById(actor, currentFileUUID);
		String originalFileName = originalEntry.getName();
		
		DocumentEntry documentEntry = documentEntryService.updateDocumentEntry(actor, currentFileUUID, file, size, fileName);
		if(documentEntry.isShared()){
			//send email file has been replaced ....
			shareService.sendSharedUpdateDocNotification(actor, documentEntry, friendlySize, originalFileName, mailContainer);
		}
           
		return documentEntryTransformer.disassemble(documentEntry);
	}
	
	@Override
    public void renameFile(String userlogin, String docEntryUuid, String newName) {
		Account actor = accountService.findByLsUid(userlogin);
        try {
			documentEntryService.renameDocumentEntry(actor, docEntryUuid, newName);
		} catch (BusinessException e) {
			logger.error("Can't rename document : " + docEntryUuid + " : " + e.getMessage());
		}
    }
	
	
	@Override
    public void  updateFileProperties(String userlogin, String docEntryUuid, String newName, String comment){
		Account actor = accountService.findByLsUid(userlogin);
		if(comment == null) {
			comment = "";
		}
        try {
			documentEntryService.updateFileProperties(actor, docEntryUuid, newName, comment);
		} catch (BusinessException e) {
			logger.error("Can't update file properties document : " + docEntryUuid + " : " + e.getMessage());
		}
    }
    
	
	@Override
    public InputStream getDocumentThumbnail(String actorUuid, String docEntryUuid) {
		if(actorUuid == null) {
			logger.error("Can't find user with null parameter.");
			return null;
		}
		
		Account actor = accountService.findByLsUid(actorUuid);
		if(actor == null) {
			logger.error("Can't find logged user.");
			return null;
		}
		
		try {
			return documentEntryService.getDocumentThumbnailStream(actor, docEntryUuid);
		} catch (BusinessException e) {
			logger.error("Can't get document thumbnail : " + docEntryUuid + " : " + e.getMessage());
		}
    	return null;
    }

	
	@Override
    public boolean documentHasThumbnail(String actorUuid, String docEntryUuid) {
		if(actorUuid == null) {
			logger.error("Can't find user with null parameter.");
			return false;
		}
		
		Account actor = accountService.findByLsUid(actorUuid);
		if(actor == null) {
			logger.error("Can't find logged user.");
			return false;
		}
		return documentEntryService.documentHasThumbnail(actor, docEntryUuid);
    }

	
	@Override
	public boolean isSignatureActive(UserVo user) {
//		User currentUser =  userRepository.findByLsUid(user.getLogin());
//		return documentService.isSignatureActive(currentUser);
		// TODO : Fix enciphermentService
		return false;
	}
	
	
	@Override
	public boolean isEnciphermentActive(UserVo user) {
//		User currentUser =  userRepository.findByLsUid(user.getLogin());
//		return documentService.isEnciphermentActive(currentUser);
		// TODO : Fix enciphermentService
		return false;
	}
	

	@Override
	public boolean isGlobalQuotaActive(UserVo userVo) throws BusinessException {
		Account actor = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.isGlobalQuotaActive(actor);
	}

	@Override
	public boolean isUserQuotaActive(UserVo userVo) throws BusinessException {
		Account actor = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.isUserQuotaActive(actor);
	}

	
	@Override
	public Long getGlobalQuota(UserVo userVo) throws BusinessException {
		Account actor = accountService.findByLsUid(userVo.getLsUid());
		return documentEntryService.getGlobalQuota(actor);
	}
	
}