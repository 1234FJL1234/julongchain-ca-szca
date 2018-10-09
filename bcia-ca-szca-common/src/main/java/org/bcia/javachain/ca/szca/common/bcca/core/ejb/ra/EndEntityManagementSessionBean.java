

package org.bcia.javachain.ca.szca.common.bcca.core.ejb.ra;

import java.awt.print.PrinterException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InvalidNameException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.approval.ApprovalSessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.authorization.ComplexAccessControlSessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.keyrecovery.KeyRecoverySessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalExecutorUtil;
import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalOveradableClassName;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.approval.ApprovalSessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.authorization.ComplexAccessControlSessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.ejb.keyrecovery.KeyRecoverySessionLocal;
import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalExecutorUtil;
import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalOveradableClassName;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.cesecore.ErrorCode;
import org.cesecore.audit.enums.EventStatus;
import org.cesecore.audit.enums.EventTypes;
import org.cesecore.audit.enums.ServiceTypes;
import org.cesecore.authentication.tokens.AlwaysAllowLocalAuthenticationToken;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.authentication.tokens.X509CertificateAuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.authorization.control.StandardRules;
import org.cesecore.authorization.user.AccessUserAspectData;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.IllegalNameException;
import org.cesecore.certificates.ca.X509CAInfo;
import org.cesecore.certificates.certificate.CertificateConstants;
import org.cesecore.certificates.certificate.CertificateData;
import org.cesecore.certificates.certificate.CertificateDataWrapper;
import org.cesecore.certificates.certificate.CertificateRevokeException;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.certificates.endentity.EndEntityConstants;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.endentity.EndEntityType;
import org.cesecore.certificates.endentity.EndEntityTypes;
import org.cesecore.certificates.endentity.ExtendedInformation;
import org.cesecore.certificates.util.DnComponents;
import org.cesecore.config.GlobalCesecoreConfiguration;
import org.cesecore.util.CeSecoreNameStyle;
import org.cesecore.util.CertTools;
import org.cesecore.util.PrintableStringNameStyle;
import org.cesecore.util.StringTools;
import org.ejbca.config.GlobalConfiguration;
import org.ejbca.config.WebConfiguration;
import org.ejbca.core.EjbcaException;
import org.ejbca.core.ejb.audit.enums.EjbcaEventTypes;
import org.ejbca.core.ejb.audit.enums.EjbcaModuleTypes;
import org.ejbca.core.ejb.authentication.cli.CliUserAccessMatchValue;
import org.ejbca.core.ejb.ca.publisher.PublisherQueueData;
import org.ejbca.core.ejb.ca.store.CertReqHistoryData;
import org.ejbca.core.ejb.hardtoken.HardTokenData;
import org.ejbca.core.ejb.keyrecovery.KeyRecoveryData;
import org.ejbca.core.ejb.ra.EndEntityExistsException;
import org.ejbca.core.ejb.ra.NoSuchEndEntityException;
import org.ejbca.core.ejb.ra.UserData;
import org.ejbca.core.model.InternalEjbcaResources;
import org.ejbca.core.model.approval.ApprovalException;
 
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.approval.approvalrequests.AddEndEntityApprovalRequest;
import org.ejbca.core.model.approval.approvalrequests.ChangeStatusEndEntityApprovalRequest;
import org.ejbca.core.model.approval.approvalrequests.EditEndEntityApprovalRequest;
import org.ejbca.core.model.approval.approvalrequests.RevocationApprovalRequest;
import org.ejbca.core.model.authorization.AccessRulesConstants;
import org.ejbca.core.model.ca.publisher.PublisherConst;
import org.ejbca.core.model.ca.publisher.PublisherQueueVolatileInformation;
import org.ejbca.core.model.ca.store.CertReqHistory;
import org.ejbca.core.model.ra.AlreadyRevokedException;
import org.ejbca.core.model.ra.CustomFieldException;
import org.ejbca.core.model.ra.EndEntityInformationFiller;
import org.ejbca.core.model.ra.ExtendedInformationFields;
import org.ejbca.core.model.ra.FieldValidator;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.model.ra.RevokeBackDateNotAllowedForProfileException;
import org.ejbca.core.model.ra.UserNotificationParamGen;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.core.model.ra.raadmin.ICustomNotificationRecipient;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.core.model.ra.raadmin.UserNotification;
import org.ejbca.util.PrinterManager;
import org.ejbca.util.dn.DistinguishedName;
import org.ejbca.util.mail.MailSender;
import org.ejbca.util.query.BasicMatch;
import org.ejbca.util.query.IllegalQueryException;
import org.ejbca.util.query.Query;
import org.ejbca.util.query.UserMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalExecutorUtil;
import org.bcia.javachain.ca.szca.common.bcca.core.model.approval.ApprovalOveradableClassName;

 


@Service
public class EndEntityManagementSessionBean implements EndEntityManagementSessionLocal {

    private   final Logger log = Logger.getLogger(EndEntityManagementSessionBean.class);
    /** Internal localization of logs and errors */
    private   final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.authorization.control.AccessControlSessionLocal authorizationSession;
    @Autowired
    private ApprovalSessionLocal approvalSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.bcca.core.ejb.ca.caadmin.CAAdminSessionLocal caAdminSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.certificates.ca.CaSessionLocal caSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.certificates.certificateprofile.CertificateProfileSessionLocal certificateProfileSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.certificates.certificate.CertificateStoreSessionLocal certificateStoreSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.bcca.core.ejb.ca.store.CertReqHistorySessionLocal certreqHistorySession;
    @Autowired
    private ComplexAccessControlSessionLocal complexAccessControlSession;
    @Autowired
    private EndEntityAccessSessionLocal endEntityAccessSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.bcca.core.ejb.ra.raadmin.EndEntityProfileSessionLocal endEntityProfileSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.configuration.GlobalConfigurationSessionLocal globalConfigurationSession;
    @Autowired
    private KeyRecoverySessionLocal keyRecoverySession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.bcca.core.ejb.ca.revoke.RevocationSessionLocal revocationSession;
    @Autowired
    private org.bcia.javachain.ca.szca.common.cesecore.audit.log.SecurityEventsLoggerSessionLocal auditSession;

    /** Columns in the database used in select. */
    private static final String USERDATA_CREATED_COL = "timeCreated";

    /** Gets the Global Configuration from ra admin session bean */
    private GlobalConfiguration getGlobalConfiguration() {
    	 
        return (GlobalConfiguration) globalConfigurationSession.getCachedConfiguration(GlobalConfiguration.GLOBAL_CONFIGURATION_ID);
    }
    
    private GlobalCesecoreConfiguration getGlobalCesecoreConfiguration() {
        return (GlobalCesecoreConfiguration) globalConfigurationSession.getCachedConfiguration(GlobalCesecoreConfiguration.CESECORE_CONFIGURATION_ID);
    }

    private boolean authorizedToCA(final AuthenticationToken admin, final int caid) {
        boolean returnval = false;
        returnval = authorizationSession.isAuthorizedNoLogging(admin, StandardRules.CAACCESS.resource() + caid);
        if (!returnval) {
            log.info("Admin " + admin.toString() + " not authorized to resource " + StandardRules.CAACCESS.resource() + caid);
        }
        return returnval;
    }

    /** Checks CA authorization and logs an official error if not and throws and AuthorizationDeniedException.
     * Does not log access control granted if granted
     */
    private void assertAuthorizedToCA(final AuthenticationToken admin, final int caid) throws AuthorizationDeniedException {
        if (!authorizedToCA(admin, caid)) {
            final String msg = intres.getLocalizedMessage("ra.errorauthca", Integer.valueOf(caid), admin.toString());
            final Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            auditSession.log(EventTypes.ACCESS_CONTROL, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    String.valueOf(caid), null, null, details);
            throw new AuthorizationDeniedException(msg);
        }
    }

    private boolean authorizedToEndEntityProfile(final AuthenticationToken admin, final int profileid, final String rights) {
        return  authorizationSession.isAuthorized(admin, AccessRulesConstants.ENDENTITYPROFILEPREFIX + profileid + rights, AccessRulesConstants.REGULAR_RAFUNCTIONALITY + rights);
    }

    /** Checks EEP authorization and logs an official error if not and throws and AuthorizationDeniedException. 
     * Logs the access control granted if granted. 
     */
    private void assertAuthorizedToEndEntityProfile(final AuthenticationToken admin, final int endEntityProfileId, final String accessRule,
            final int caId) throws AuthorizationDeniedException {
        if (!authorizedToEndEntityProfile(admin, endEntityProfileId, accessRule)) {
            final String msg = intres.getLocalizedMessage("ra.errorauthprofile", Integer.valueOf(endEntityProfileId), admin.toString());
            final Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            auditSession.log(EventTypes.ACCESS_CONTROL, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    String.valueOf(caId), null, null, details);
            throw new AuthorizationDeniedException(msg);
        }
    }

    @Override
    public void addUser(final AuthenticationToken admin, final String username, final String password, final String subjectdn, final String subjectaltname, final String email,
            final boolean clearpwd, final int endentityprofileid, final int certificateprofileid, final EndEntityType type, final int tokentype, final int hardwaretokenissuerid, final int caid)
            throws EndEntityExistsException, AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, WaitingForApprovalException,
            CADoesntExistsException, EjbcaException {
        final EndEntityInformation userdata = new EndEntityInformation(username, subjectdn, caid, subjectaltname, email, EndEntityConstants.STATUS_NEW,
                type, endentityprofileid, certificateprofileid, null, null, tokentype, hardwaretokenissuerid, null);
        userdata.setPassword(password);
        addUser(admin, userdata, clearpwd);
    }

    private   final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_ADDUSER = { new ApprovalOveradableClassName(
            org.ejbca.core.model.approval.approvalrequests.AddEndEntityApprovalRequest.class.getName(), null), };

    @Override
    public void addUserFromWS(final AuthenticationToken admin, EndEntityInformation userdata, final boolean clearpwd) throws AuthorizationDeniedException,
            UserDoesntFullfillEndEntityProfile, EndEntityExistsException, WaitingForApprovalException, CADoesntExistsException, EjbcaException {
        final int profileId = userdata.getEndEntityProfileId();
        final EndEntityProfile profile = endEntityProfileSession.getEndEntityProfileNoClone(profileId);
        if (profile.getAllowMergeDnWebServices()) {
            userdata = EndEntityInformationFiller.fillUserDataWithDefaultValues(userdata, profile);
        }
        addUser(admin, userdata, clearpwd);
    }

    @Override
    public void canonicalizeUser(final EndEntityInformation endEntity) throws EjbcaException {
        final int endEntityProfileId = endEntity.getEndEntityProfileId();
        final String endEntityProfileName = endEntityProfileSession.getEndEntityProfileName(endEntityProfileId);
        try {
			FieldValidator.validate(endEntity, endEntityProfileId, endEntityProfileName);
		} catch (CustomFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        final String dn = CertTools.stringToBCDNString(StringTools.strip(endEntity.getDN()));
        endEntity.setDN(dn);
        endEntity.setSubjectAltName(StringTools.strip(endEntity.getSubjectAltName()));
        endEntity.setEmail(StringTools.strip(endEntity.getEmail()));
    }
    
     @Transactional 
     @Override
     public void addUser(final AuthenticationToken admin, final EndEntityInformation endEntity, final boolean clearpwd) throws AuthorizationDeniedException,
            EjbcaException, EndEntityExistsException, UserDoesntFullfillEndEntityProfile, WaitingForApprovalException, CADoesntExistsException {
        final int endEntityProfileId = endEntity.getEndEntityProfileId();
        final int caid = endEntity.getCAId();
        // Check if administrator is authorized to add user to CA.
        assertAuthorizedToCA(admin, caid);
        final GlobalConfiguration globalConfiguration = getGlobalConfiguration();
        if (globalConfiguration.getEnableEndEntityProfileLimitations()) {
            // Check if administrator is authorized to add user.
            assertAuthorizedToEndEntityProfile(admin, endEntityProfileId, AccessRulesConstants.CREATE_END_ENTITY, caid);
        }
        
        final String originalDN = endEntity.getDN();
        canonicalizeUser(endEntity);
        if (log.isTraceEnabled()) {
            log.trace(">addUser(" + endEntity.getUsername() + ", password, " + endEntity.getDN() + ", " + originalDN + ", " + endEntity.getSubjectAltName()
                    + ", " + endEntity.getEmail() + ", profileId: " + endEntityProfileId + ")");
        }
        
        final String endEntityProfileName = endEntityProfileSession.getEndEntityProfileName(endEntityProfileId);
        final String username = endEntity.getUsername();
        final String dn = endEntity.getDN();
        final String altName = endEntity.getSubjectAltName();
        final String email = endEntity.getEmail();
        final EndEntityType type = endEntity.getType();
        String newpassword = endEntity.getPassword();
        EndEntityProfile profile = null; // Only look this up if we need it..
        if (endEntity.getPassword() == null) {
            profile = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
            if (profile.useAutoGeneratedPasswd()) {
                // special case used to signal regeneration of password
                newpassword = profile.getAutoGeneratedPasswd();
            }
        }
        if (globalConfiguration.getEnableEndEntityProfileLimitations()) {
            if (profile == null) {
                profile = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
            }
            // Check if user fulfills it's profile.
            try {
                final String dirattrs = endEntity.getExtendedinformation() != null ? endEntity.getExtendedinformation()
                        .getSubjectDirectoryAttributes() : null;
                profile.doesUserFullfillEndEntityProfile(username, endEntity.getPassword(), dn, altName, dirattrs, email,
                        endEntity.getCertificateProfileId(), clearpwd, type.contains(EndEntityTypes.KEYRECOVERABLE),
                        type.contains(EndEntityTypes.SENDNOTIFICATION), endEntity.getTokenType(), endEntity.getHardTokenIssuerId(), caid,
                        endEntity.getExtendedinformation());
            } catch (UserDoesntFullfillEndEntityProfile e) {
                final String msg = intres.getLocalizedMessage("ra.errorfullfillprofile", endEntityProfileName, dn, e.getMessage());
                Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", msg);
                auditSession.log(EjbcaEventTypes.RA_ADDENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
                throw e;
            }
        }
        // Get CAInfo, to be able to read configuration
        // No need to access control on the CA here just to get these flags, we have already checked above that we are authorized to the CA
        final CAInfo caInfo = caSession.getCAInfoInternal(caid, null, true);
        // Check if approvals is required. (Only do this if store users, otherwise this approval is disabled.)
        if (caInfo.isUseUserStorage()) {
            final int numOfApprovalsRequired = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_ADDEDITENDENTITY, caid,
                    endEntity.getCertificateProfileId());
            if (numOfApprovalsRequired > 0) {
                AddEndEntityApprovalRequest ar = new AddEndEntityApprovalRequest(endEntity, clearpwd, admin, null, numOfApprovalsRequired, caid,
                        endEntityProfileId);
                if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_ADDUSER)) {
                    approvalSession.addApprovalRequest(admin, ar);
                    throw new WaitingForApprovalException(intres.getLocalizedMessage("ra.approvalad"));
                }
            }
        }
        // Check if the subjectDN serialnumber already exists.
        if (caInfo.isDoEnforceUniqueSubjectDNSerialnumber()) {
            if (caInfo.isUseUserStorage()) {
                if (!isSubjectDnSerialnumberUnique(caid, dn, username)) {
                    throw new EjbcaException(ErrorCode.SUBJECTDN_SERIALNUMBER_ALREADY_EXISTS, "Error: SubjectDN Serialnumber already exists.");
                }
            } else {
                log.warn("CA configured to enforce unique SubjectDN serialnumber, but not to store any user data. Check will be ignored. Please verify your configuration.");
            }
        }
        // Check name constraints
        if (caInfo instanceof X509CAInfo && caInfo.getCertificateChain() != null && !caInfo.getCertificateChain().isEmpty()) {
            final X509CAInfo x509cainfo = (X509CAInfo) caInfo;
            final X509Certificate cacert = (X509Certificate)caInfo.getCertificateChain().iterator().next();
            final CertificateProfile certProfile = certificateProfileSession.getCertificateProfile(endEntity.getCertificateProfileId());
            
            final X500NameStyle nameStyle;
            if (x509cainfo.getUsePrintableStringSubjectDN()) {
                nameStyle = PrintableStringNameStyle.INSTANCE;
            } else {
                nameStyle = CeSecoreNameStyle.INSTANCE;
            }
            
            final boolean ldaporder;
            if (x509cainfo.getUseLdapDnOrder() && certProfile.getUseLdapDnOrder()) {
                ldaporder = true; // will cause an error to be thrown later if name constraints are used
            } else {
                ldaporder = false;
            }
            
            X500Name subjectDNName = CertTools.stringToBcX500Name(dn, nameStyle, ldaporder);
            GeneralNames subjectAltName = CertTools.getGeneralNamesFromAltName(altName);
            try {
                CertTools.checkNameConstraints(cacert, subjectDNName, subjectAltName);
            } catch (IllegalNameException e) {
                throw new EjbcaException(ErrorCode.NAMECONSTRAINT_VIOLATION, e.getMessage());
            }
        }
        // Store a new UserData in the database, if this CA is configured to do so.
        if (caInfo.isUseUserStorage()) {
            try {
                // Create the user in one go with all parameters at once. This was important in EJB2.1 so the persistence layer only creates *one*
                // single
                // insert statement. If we do a home.create and the some setXX, it will create one insert and one update statement to the database.
                // Probably not important in EJB3 anymore.
                final UserData userData = new UserData(username, newpassword, clearpwd, dn, caid, endEntity.getCardNumber(), altName, email, type.getHexValue(),
                        endEntityProfileId, endEntity.getCertificateProfileId(), endEntity.getTokenType(), endEntity.getHardTokenIssuerId(),
                        endEntity.getExtendedinformation());
                // Since persist will not commit and fail if the user already exists, we need to check for this
                // Flushing the entityManager will not allow us to rollback the persisted user if this is a part of a larger transaction.
                if (UserData.findByUsername(entityManager, userData.getUsername()) != null) {
                    throw new EndEntityExistsException("User " + userData.getUsername() + " already exists.");
                }
                entityManager.persist(userData);
                // Although EndEntityInformation should always have a null password for
                // autogenerated end entities, the notification framework
                // expect it to exist. Since nothing else but printing is done after
                // this point it is safe to set the password
                endEntity.setPassword(newpassword);
                // Send notifications, if they should be sent
                sendNotification(admin, endEntity, EndEntityConstants.STATUS_NEW);
                if (type.contains(EndEntityTypes.PRINT)) {
                    if (profile == null) {
                        profile = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
                    }
                    print(profile, endEntity);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Type ("+type.getHexValue()+") does not contain SecConst.USER_PRINT, no print job created.");
                    }
                }
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", intres.getLocalizedMessage("ra.addedentity", username));
                auditSession.log(EjbcaEventTypes.RA_ADDENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
            } catch (EndEntityExistsException e) {
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", intres.getLocalizedMessage("ra.errorentityexist", username));
                details.put("error", e.getMessage());
                auditSession.log(EjbcaEventTypes.RA_ADDENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
                throw e;
            } catch (Exception e) {
                final String msg = intres.getLocalizedMessage("ra.erroraddentity", username);
                log.error(msg, e);
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", msg);
                details.put("error", e.getMessage());
                auditSession.log(EjbcaEventTypes.RA_ADDENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
                throw new EJBException(e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<addUser(" + username + ", password, " + dn + ", " + email + ")");
        }
    }

    /* Does not check authorization. Calling code is responsible for this. */
    private boolean isSubjectDnSerialnumberUnique(final int caid, final String subjectDN, final String username) {
        final String serialnumber = CertTools.getPartFromDN(subjectDN, "SN");
        if (log.isDebugEnabled()) {
            log.debug("subjectDN=" + subjectDN + " extracted SN=" + serialnumber);
        }
        // We treat the lack of a serialnumber field as unique
        if (serialnumber == null) {
            return true;
        }
        // Without a username we cannot determine if this is the same user, if we find any in the database later
        if (username == null) {
            return false;
        }
        final List<String> subjectDNs = UserData.findSubjectDNsByCaIdAndNotUsername(entityManager, caid, username, serialnumber);
        // Even though we push down most of the work to the database we still have to verify the serialnumber here since
        // for example serialnumber '1' will match both "SN=1" and "SN=10" etc
        for (final String currentSubjectDN : subjectDNs) {
            final String currentSn = CertTools.getPartFromDN(currentSubjectDN, "SN");
            if (serialnumber.equals(currentSn)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Help method that checks the CA data config if specified action requires approvals and how many
     * 
     * @param action one of CAInfo.REQ_APPROVAL_ constants
     * @param caid of the ca to check
     * @param certprofileid of the certificate profile to check
     * 
     * @return 0 of no approvals is required or no such CA exists, otherwise the number of approvals
     */
    private int getNumOfApprovalRequired(final int action, final int caid, final int certprofileid) {
        return caAdminSession.getNumOfApprovalRequired(action, caid, certprofileid);
    }

    @SuppressWarnings("unchecked")
    @Transactional 
    @Override
    public boolean renameEndEntity(final AuthenticationToken admin, String currentUsername, String newUsername) throws AuthorizationDeniedException, EndEntityExistsException {
        // Sanity check parameters
        if (currentUsername==null || newUsername==null) {
            throw new IllegalArgumentException("Cannot rename an end entity to or from null.");
        }
        currentUsername = StringTools.stripUsername(currentUsername).trim();
        newUsername = StringTools.stripUsername(newUsername).trim();
        if (currentUsername.length()==0 || newUsername.length()==0) {
            throw new IllegalArgumentException("Cannot rename an end entity to or from empty string.");
        }
        // Check that end entity exists and that the target username isn't already in use
        final UserData currentUserData = UserData.findByUsername(entityManager, currentUsername);
        if (currentUserData==null) {
            return false;
        }
        if (UserData.findByUsername(entityManager, newUsername)!=null) {
            throw new EndEntityExistsException("Unable to rename end entity, since end entity with username '" + newUsername + "' already exists.");
        }
        // Check authorization
        final int currentCaId = currentUserData.getCaId();
        assertAuthorizedToCA(admin, currentCaId);
        final GlobalConfiguration globalConfiguration = getGlobalConfiguration();
        if (globalConfiguration.getEnableEndEntityProfileLimitations()) {
            // Check if administrator is authorized to edit user.
            assertAuthorizedToEndEntityProfile(admin, currentUserData.getEndEntityProfileId(), AccessRulesConstants.EDIT_END_ENTITY, currentCaId);
        }
        // Rename the end entity. Username is a primary key of the UserData table and we need to use JPA for this to get rowProtection.
        // we need to add a new end entity and remove the old one.
        final long now = System.currentTimeMillis();
        final UserData userDataClone = currentUserData.clone();
        userDataClone.setUsername(newUsername);
        userDataClone.setTimeModified(now);
        entityManager.persist(userDataClone);
        entityManager.remove(currentUserData);
        // Find all entities and update the username (we cant just do UPDATE ... SET username.. WHERE username since rowProtection might be enabled)
        final List<CertificateData> certificateDatas = (List<CertificateData>) entityManager.createQuery(
                "SELECT a FROM CertificateData a WHERE a.username=:username").setParameter("username", currentUsername).getResultList();
        int updatedPublisherQueueDataRows = 0;
        for (final CertificateData certificateData : certificateDatas) {
            final String fingerprint = certificateData.getFingerprint();
            certificateData.setUsername(newUsername);
            certificateData.setUpdateTime(now);
            // Find all publisher queue data where PublisherQueueData.fingerprint matches CertificateData.fingerprint for this user
            final List<PublisherQueueData> publisherQueueDatas = PublisherQueueData.findDataByFingerprint(entityManager, fingerprint);
            for (final PublisherQueueData publisherQueueData : publisherQueueDatas) {
                // Only process entries that has not yet been published/processed
                if (publisherQueueData.getPublishStatus()==PublisherConst.STATUS_PENDING) {
                    final PublisherQueueVolatileInformation volatileInformation = publisherQueueData.getPublisherQueueVolatileData();
                    if (currentUsername.equals(volatileInformation.getUsername())) {
                        volatileInformation.setUsername(newUsername);
                        // Invoke setter to trigger update of still managed JPA entity
                        publisherQueueData.setPublisherQueueVolatileData(volatileInformation);
                        updatedPublisherQueueDataRows++;
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + certificateDatas.size() + " rows of CertificateData.");
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + updatedPublisherQueueDataRows + " rows of PublisherQueueData.");
        }
        final List<CertReqHistoryData> certReqHistoryDatas = (List<CertReqHistoryData>) entityManager.createQuery(
                "SELECT a FROM CertReqHistoryData a WHERE a.username=:username").setParameter("username", currentUsername).getResultList();
        for (final CertReqHistoryData current : certReqHistoryDatas) {
            // Note: Ignore the username inside certReqHistoryData.getUserDataVO(), since this should reflect the state of the UserData at the time of certificate issuance
            current.setUsername(newUsername);
        }
        if (log.isDebugEnabled()) {
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + certReqHistoryDatas.size() + " rows of CertReqHistoryData.");
        }
        final List<KeyRecoveryData> keyRecoveryDatas = (List<KeyRecoveryData>) entityManager.createQuery(
                "SELECT a FROM KeyRecoveryData a WHERE a.username=:username").setParameter("username", currentUsername).getResultList();
        for (final KeyRecoveryData current : keyRecoveryDatas) {
            current.setUsername(newUsername);
        }
        if (log.isDebugEnabled()) {
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + keyRecoveryDatas.size() + " rows of KeyRecoveryData.");
        }
        final List<HardTokenData> hardTokenDatas = (List<HardTokenData>) entityManager.createQuery(
                "SELECT a FROM HardTokenData a WHERE a.username=:username").setParameter("username", currentUsername).getResultList();
        for (final HardTokenData current : hardTokenDatas) {
            current.setUsername(newUsername);
        }
        if (log.isDebugEnabled()) {
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + hardTokenDatas.size() + " rows of HardTokenData.");
        }
        // Update CLI admins where this username is used in AdminEntityData table.
        final List<AccessUserAspectData> accessUserAspectDatas = (List<AccessUserAspectData>) entityManager.createQuery(
                "SELECT a FROM AccessUserAspectData a WHERE a.tokenType=:tokenType AND a.matchWith=:matchWith AND a.matchValue=:matchValue")
                .setParameter("tokenType", CliUserAccessMatchValue.USERNAME.getTokenType())
                .setParameter("matchWith", CliUserAccessMatchValue.USERNAME.getNumericValue())
                .setParameter("matchValue", currentUsername)
                .getResultList();
        for (final AccessUserAspectData current : accessUserAspectDatas) {
            current.setMatchValue(newUsername);
        }
        if (log.isDebugEnabled()) {
            log.debug("Changed username '" + currentUsername + "' to '" + newUsername + "' in " + accessUserAspectDatas.size() + " rows of AdminEntityData.");
        }
        final String msg = intres.getLocalizedMessage("ra.editedentityrename", currentUsername, newUsername);
        auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                String.valueOf(currentCaId), newUsername, currentUsername, msg);
        return true;
    }

    @Deprecated
    @Override
    public void changeUser(final AuthenticationToken admin, final String username, final String password, final String subjectdn, final String subjectaltname, final String email,
            final boolean clearpwd, final int endentityprofileid, final int certificateprofileid, final EndEntityType type, final int tokentype, final int hardwaretokenissuerid, final int status,
            final int caid) throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, WaitingForApprovalException, CADoesntExistsException,
            EjbcaException {
        final EndEntityInformation userdata = new EndEntityInformation(username, subjectdn, caid, subjectaltname, email, status, type, endentityprofileid,
                certificateprofileid, null, null, tokentype, hardwaretokenissuerid, null);
        userdata.setPassword(password);
        changeUser(admin, userdata, clearpwd, false);
    }

    private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_CHANGEUSER = {
            new ApprovalOveradableClassName(org.ejbca.core.model.approval.approvalrequests.EditEndEntityApprovalRequest.class.getName(), null),
            /**
             * can not use .class.getName() below, because it is not part of base EJBCA dist
             */
            new ApprovalOveradableClassName("se.primeKey.cardPersonalization.ra.connection.ejbca.EjbcaConnection", null) };
    @Transactional 
    @Override
    public void changeUser(final AuthenticationToken admin, final EndEntityInformation userdata, final boolean clearpwd) throws AuthorizationDeniedException,
            UserDoesntFullfillEndEntityProfile, WaitingForApprovalException, CADoesntExistsException, EjbcaException {
        changeUser(admin, userdata, clearpwd, false);
    }
    @Transactional 
    @Override
    public void changeUser(final AuthenticationToken admin, final EndEntityInformation endEntityInformation, final boolean clearpwd,
            final boolean fromWebService) throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, WaitingForApprovalException,
            CADoesntExistsException, EjbcaException {
        final int endEntityProfileId = endEntityInformation.getEndEntityProfileId();
        final int caid = endEntityInformation.getCAId();
        final String username = endEntityInformation.getUsername();
        // Check if administrator is authorized to edit user to CA.
        assertAuthorizedToCA(admin, caid);
        final GlobalConfiguration globalConfiguration = getGlobalConfiguration();
        if (globalConfiguration.getEnableEndEntityProfileLimitations()) {
            // Check if administrator is authorized to edit user.
            assertAuthorizedToEndEntityProfile(admin, endEntityProfileId, AccessRulesConstants.EDIT_END_ENTITY, caid);
        }
        try {
			FieldValidator.validate(endEntityInformation, endEntityProfileId, endEntityProfileSession.getEndEntityProfileName(endEntityProfileId));
		} catch (CustomFieldException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String dn = CertTools.stringToBCDNString(StringTools.strip(endEntityInformation.getDN()));
        String altName = endEntityInformation.getSubjectAltName();
        if (log.isTraceEnabled()) {
            log.trace(">changeUser(" + username + ", " + dn + ", " + endEntityInformation.getEmail() + ")");
        }
        final UserData userData = UserData.findByUsername(entityManager, username);
        if (userData == null) {
            final String msg = intres.getLocalizedMessage("ra.erroreditentity", username);
            log.info(msg);
            throw new EJBException(msg);
        }
        final EndEntityProfile profile = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
        // if required, we merge the existing user dn into the dn provided by the web service.
        if (fromWebService && profile.getAllowMergeDnWebServices()) {
            if (userData != null) {
                if (userData.getSubjectDN() != null) {
                    final Map<String, String> dnMap = new HashMap<String, String>();
                    if (profile.getUse(DnComponents.DNEMAILADDRESS, 0)) {
                        dnMap.put(DnComponents.DNEMAILADDRESS, endEntityInformation.getEmail());
                    }
                    try {
                        dn = (new DistinguishedName(userData.getSubjectDN())).mergeDN(new DistinguishedName(dn), true, dnMap).toString();
                    } catch (InvalidNameException e) {
                        log.debug("Invalid dn. We make it empty");
                        dn = "";
                    }
                }
                if (userData.getSubjectAltName() != null) {
                    final Map<String, String> dnMap = new HashMap<String, String>();
                    if (profile.getUse(DnComponents.RFC822NAME, 0)) {
                        dnMap.put(DnComponents.RFC822NAME, endEntityInformation.getEmail());
                    }
                    try {
                        // SubjectAltName is not mandatory so
                        if (altName == null) {
                            altName = "";
                        }
                        altName = (new DistinguishedName(userData.getSubjectAltName())).mergeDN(new DistinguishedName(altName), true, dnMap)
                                .toString();
                    } catch (InvalidNameException e) {
                        log.debug("Invalid altName. We make it empty");
                        altName = "";
                    }
                }
            }
        }
        String newpassword = endEntityInformation.getPassword();
        if (profile.useAutoGeneratedPasswd() && newpassword != null) {
            // special case used to signal regeneraton of password
            newpassword = profile.getAutoGeneratedPasswd();
        }

        final EndEntityType type = endEntityInformation.getType();
        final ExtendedInformation ei = endEntityInformation.getExtendedinformation();
        // Check if user fulfills it's profile.
        if (globalConfiguration.getEnableEndEntityProfileLimitations()) {
            try {
                String dirattrs = null;
                if (ei != null) {
                    dirattrs = ei.getSubjectDirectoryAttributes();
                }
                // It is only meaningful to verify the password if we change it in some way, and if we are not autogenerating it
                if (!profile.useAutoGeneratedPasswd() && StringUtils.isNotEmpty(newpassword)) {
                    profile.doesUserFullfillEndEntityProfile(username, endEntityInformation.getPassword(), dn, altName, dirattrs, endEntityInformation.getEmail(),
                            endEntityInformation.getCertificateProfileId(), clearpwd, type.contains(EndEntityTypes.KEYRECOVERABLE),
                            type.contains(EndEntityTypes.SENDNOTIFICATION), endEntityInformation.getTokenType(), endEntityInformation.getHardTokenIssuerId(), caid, ei);
                } else {
                    profile.doesUserFullfillEndEntityProfileWithoutPassword(username, dn, altName, dirattrs, endEntityInformation.getEmail(),
                            endEntityInformation.getCertificateProfileId(), type.contains(EndEntityTypes.KEYRECOVERABLE),
                            type.contains(EndEntityTypes.SENDNOTIFICATION), endEntityInformation.getTokenType(), endEntityInformation.getHardTokenIssuerId(), caid, ei);
                }
            } catch (UserDoesntFullfillEndEntityProfile e) {
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", intres.getLocalizedMessage("ra.errorfullfillprofile", Integer.valueOf(endEntityProfileId), dn, e.getMessage()));
                auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
                throw e;
            }
        }
        // Check if approvals is required.
        final int numOfApprovalsRequired = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_ADDEDITENDENTITY, caid, endEntityInformation.getCertificateProfileId());
        if (numOfApprovalsRequired > 0) {
            final EndEntityInformation orguserdata = userData.toEndEntityInformation();
            final EditEndEntityApprovalRequest ar = new EditEndEntityApprovalRequest(endEntityInformation, clearpwd, orguserdata, admin, null,
                    numOfApprovalsRequired, caid, endEntityProfileId);
            if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_CHANGEUSER)) {
                approvalSession.addApprovalRequest(admin, ar);
                throw new WaitingForApprovalException(intres.getLocalizedMessage("ra.approvaledit"));
            }
        }
        // Check if the subjectDN serialnumber already exists.
        // No need to access control on the CA here just to get these flags, we have already checked above that we are authorized to the CA
        CAInfo cainfo = caSession.getCAInfoInternal(caid, null, true);
        if (cainfo.isDoEnforceUniqueSubjectDNSerialnumber()) {
            if (!isSubjectDnSerialnumberUnique(caid, dn, username)) {
                throw new EjbcaException(ErrorCode.SUBJECTDN_SERIALNUMBER_ALREADY_EXISTS, "Error: SubjectDN Serialnumber already exists.");
            }
        }
        // Check name constraints
        final boolean nameChanged = // only check when name is changed so existing end-entities can be changed even if they violate NCs
            !userData.getSubjectDN().equals(CertTools.stringToBCDNString(dn)) ||
            (userData.getSubjectAltName() != null && !userData.getSubjectAltName().equals(altName));
        if (nameChanged && cainfo instanceof X509CAInfo && !cainfo.getCertificateChain().isEmpty()) {
            final X509CAInfo x509cainfo = (X509CAInfo) cainfo;
            final X509Certificate cacert = (X509Certificate)cainfo.getCertificateChain().iterator().next();
            final CertificateProfile certProfile = certificateProfileSession.getCertificateProfile(userData.getCertificateProfileId());
            
            final X500NameStyle nameStyle;
            if (x509cainfo.getUsePrintableStringSubjectDN()) {
                nameStyle = PrintableStringNameStyle.INSTANCE;
            } else {
                nameStyle = CeSecoreNameStyle.INSTANCE;
            }
            
            final boolean ldaporder;
            if (x509cainfo.getUseLdapDnOrder() && (certProfile != null && certProfile.getUseLdapDnOrder())) {
                ldaporder = true; // will cause an error to be thrown later if name constraints are used
            } else {
                ldaporder = false;
            }
            
            X500Name subjectDNName = CertTools.stringToBcX500Name(dn, nameStyle, ldaporder);
            GeneralNames subjectAltName = CertTools.getGeneralNamesFromAltName(altName);
            try {
                CertTools.checkNameConstraints(cacert, subjectDNName, subjectAltName);
            } catch (IllegalNameException e) {
                throw new EjbcaException(ErrorCode.NAMECONSTRAINT_VIOLATION, e.getMessage());
            }
        }
        
        try {
            userData.setDN(dn);
            userData.setSubjectAltName(altName);
            userData.setSubjectEmail(endEntityInformation.getEmail());
            userData.setCaId(caid);
            userData.setType(type.getHexValue());
            userData.setEndEntityProfileId(endEntityProfileId);
            userData.setCertificateProfileId(endEntityInformation.getCertificateProfileId());
            userData.setTokenType(endEntityInformation.getTokenType());
            userData.setHardTokenIssuerId(endEntityInformation.getHardTokenIssuerId());
            userData.setCardNumber(endEntityInformation.getCardNumber());
            final int newstatus = endEntityInformation.getStatus();
            final int oldstatus = userData.getStatus();
            if (oldstatus == EndEntityConstants.STATUS_KEYRECOVERY && newstatus != EndEntityConstants.STATUS_KEYRECOVERY
                    && newstatus != EndEntityConstants.STATUS_INPROCESS) {
                keyRecoverySession.unmarkUser(admin, username);
            }
            if (ei != null) {
                final String requestCounter = ei.getCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER);
                if (StringUtils.equals(requestCounter, "0") && newstatus == EndEntityConstants.STATUS_NEW && oldstatus != EndEntityConstants.STATUS_NEW) {
                    // If status is set to new, we should re-set the allowed request counter to the default values
                    // But we only do this if no value is specified already, i.e. 0 or null
                    resetRequestCounter(admin, false, ei, username, endEntityProfileId);
                } else {
                    // If status is not new, we will only remove the counter if the profile does not use it
                    resetRequestCounter(admin, true, ei, username, endEntityProfileId);
                }
            }
            userData.setExtendedInformation(ei);
            userData.setStatus(newstatus);
            if (StringUtils.isNotEmpty(newpassword)) {
                if (clearpwd) {
                    userData.setOpenPassword(newpassword);
                } else {
                    userData.setPassword(newpassword);
                }
            }
            // We want to create this object before re-setting the time modified, because we may want to
            // use the old time modified in any notifications
            final EndEntityInformation notificationEndEntityInformation = userData.toEndEntityInformation();
            userData.setTimeModified(new Date().getTime());
            // We also want to be able to handle non-clear generated passwords in the notification, although EndEntityInformation
            // should always have a null password for autogenerated end entities the notification framework expects it to
            // exist.
            if (newpassword != null) {
                notificationEndEntityInformation.setPassword(newpassword);
            }
            // Send notification if it should be sent.
            sendNotification(admin, notificationEndEntityInformation, newstatus);
            if (newstatus != oldstatus) {
                // Only print stuff on a printer on the same conditions as for
                // notifications, we also only print if the status changes, not for
                // every time we press save
                if (type.contains(EndEntityTypes.PRINT)
                        && (newstatus == EndEntityConstants.STATUS_NEW || newstatus == EndEntityConstants.STATUS_KEYRECOVERY || newstatus == EndEntityConstants.STATUS_INITIALIZED)) {
                    print(profile, endEntityInformation);
                }
                final String msg = intres.getLocalizedMessage("ra.editedentitystatus", username, Integer.valueOf(newstatus));
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", msg);
                auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
            } else {
                final String msg = intres.getLocalizedMessage("ra.editedentity", username);
                final Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("msg", msg);
                auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                        String.valueOf(caid), null, username, details);
            }
        } catch (Exception e) {
            final String msg = intres.getLocalizedMessage("ra.erroreditentity", username);
            final Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            details.put("error", e.getMessage());
            auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    String.valueOf(caid), null, username, details);
            log.error("ChangeUser:", e);
            throw new EJBException(e);
        }
        if (log.isTraceEnabled()) {
            log.trace("<changeUser(" + username + ", password, " + dn + ", " + endEntityInformation.getEmail() + ")");
        }
    }

    @Override
    public void deleteUser(final AuthenticationToken admin, final String username) throws AuthorizationDeniedException, NotFoundException, RemoveException {
        if (log.isTraceEnabled()) {
            log.trace(">deleteUser(" + username + ")");
        }
        // Check if administrator is authorized to delete user.
        String caIdLog = null;
        final UserData data1 = UserData.findByUsername(entityManager, username);
        if (data1 != null) {
            final int caid = data1.getCaId();
            caIdLog = String.valueOf(caid);
            assertAuthorizedToCA(admin, caid);
            if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
                assertAuthorizedToEndEntityProfile(admin, data1.getEndEntityProfileId(), AccessRulesConstants.DELETE_END_ENTITY, caid);
            }
        } else {
            log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
            // This exception message is used to not leak information to the user 
            final String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
            log.info(msg);
            throw new NotFoundException(msg);
        }
        try {
            entityManager.remove(data1);
            final String msg = intres.getLocalizedMessage("ra.removedentity", username);
            final Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            auditSession.log(EjbcaEventTypes.RA_DELETEENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    caIdLog, null, username, details);
        } catch (Exception e) {
            final String msg = intres.getLocalizedMessage("ra.errorremoveentity", username);
            final Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            details.put("error", e.getMessage());
            auditSession.log(EjbcaEventTypes.RA_DELETEENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    caIdLog, null, username, details);
            throw new RemoveException(msg);
        }
        if (log.isTraceEnabled()) {
            log.trace("<deleteUser(" + username + ")");
        }
    }

    private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_SETUSERSTATUS = {
            new ApprovalOveradableClassName(org.ejbca.core.model.approval.approvalrequests.ChangeStatusEndEntityApprovalRequest.class.getName(), null),
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ra.EndEntityManagementSessionBean.class.getName(), "revokeUser"),
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ra.EndEntityManagementSessionBean.class.getName(), "revokeCert"),
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ca.auth.EndEntityAuthenticationSessionBean.class.getName(), "finishUser"),
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ra.EndEntityManagementSessionBean.class.getName(), "unrevokeCert"),
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ra.EndEntityManagementSessionBean.class.getName(), "prepareForKeyRecovery"),
            /**
             * can not use .class.getName() below, because it is not part of base EJBCA dist
             */
            new ApprovalOveradableClassName("org.ejbca.extra.caservice.ExtRACAProcess", "processExtRARevocationRequest"),
            new ApprovalOveradableClassName("se.primeKey.cardPersonalization.ra.connection.ejbca.EjbcaConnection", null) };

    @Override
    public void resetRemainingLoginAttempts(String username) throws FinderException {
        if (log.isTraceEnabled()) {
            log.trace(">resetRamainingLoginAttempts(" + username + ")");
        }
        int resetValue = -1;
        final UserData data1 = UserData.findByUsername(entityManager, username);
        if (data1 != null) {
            final int caid = data1.getCaId();
            final ExtendedInformation ei = data1.getExtendedInformation();
            if (ei != null) {
                resetRemainingLoginAttemptsInternal(ei, username, caid);
                data1.setTimeModified(new Date().getTime());
                data1.setExtendedInformation(ei);
            }
        } else {
            log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
            // This exception message is used to not leak information to the user
            String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
            log.info(msg);
            throw new FinderException(msg);
        }
        if (log.isTraceEnabled()) {
            log.trace("<resetRamainingLoginAttempts(" + username + "): " + resetValue);
        }
    }

    /**
     * Assumes authorization has already been checked.. Modifies the ExtendedInformation object to reset the remaining login attempts.
     */
    private void resetRemainingLoginAttemptsInternal(final ExtendedInformation ei, final String username,
            final int caid) {
        if (log.isTraceEnabled()) {
            log.trace(">resetRemainingLoginAttemptsInternal");
        }
        final int resetValue = ei.getMaxLoginAttempts();
        if (resetValue != -1 || ei.getRemainingLoginAttempts() != -1) {
            ei.setRemainingLoginAttempts(resetValue);
            final String msg = intres.getLocalizedMessage("ra.resettedloginattemptscounter", username, resetValue);
            log.info(msg);
        }
        if (log.isTraceEnabled()) {
            log.trace("<resetRamainingLoginAttemptsInternal: " + resetValue);
        }
    }

    @Override
    public void decRemainingLoginAttempts(String username) throws FinderException {
        if (log.isTraceEnabled()) {
            log.trace(">decRemainingLoginAttempts(" + username + ")");
        }
        int counter = Integer.MAX_VALUE;
        UserData data1 = UserData.findByUsername(entityManager, username);
        if (data1 != null) {
            final int caid = data1.getCaId();
            final ExtendedInformation ei = data1.getExtendedInformation();
            if (ei != null) {
                counter = ei.getRemainingLoginAttempts();
                // If we get to 0 we must set status to generated
                if (counter == 0) {
                    // if it isn't already
                    if (data1.getStatus() != EndEntityConstants.STATUS_GENERATED) {
                        data1.setStatus(EndEntityConstants.STATUS_GENERATED);
                        final String msg = intres.getLocalizedMessage("ra.decreasedloginattemptscounter", username, counter);
                        log.info(msg);
                        resetRemainingLoginAttemptsInternal(ei, username, caid);
                        data1.setTimeModified(new Date().getTime());
                        data1.setExtendedInformation(ei);
                    }
                } else if (counter != -1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a remaining login counter with value " + counter);
                    }
                    ei.setRemainingLoginAttempts(--counter);
                    data1.setExtendedInformation(ei);
                    String msg = intres.getLocalizedMessage("ra.decreasedloginattemptscounter", username, counter);
                    log.info(msg);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a remaining login counter with value UNLIMITED, not decreased in db.");
                    }
                    counter = Integer.MAX_VALUE;
                }
            }
        } else {
            log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
            // This exception message is used to not leak information to the user
            String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
            throw new FinderException(msg);
        }
        if (log.isTraceEnabled()) {
            log.trace("<decRemainingLoginAttempts(" + username + "): " + counter);
        }
    }

    @Override
    public int decRequestCounter(String username) throws FinderException, ApprovalException,
            WaitingForApprovalException {
        if (log.isTraceEnabled()) {
            log.trace(">decRequestCounter(" + username + ")");
        }
        // Default return value is as if the optional value does not exist for
        // the user, i.e. the default values is 0
        // because the default number of allowed requests are 1
        int counter = 0;
        // Check if administrator is authorized to edit user.
        UserData data1 = UserData.findByUsername(entityManager, username);
        if (data1 != null) {
            // Do the work of decreasing the counter
            ExtendedInformation ei = data1.getExtendedInformation();
            if (ei != null) {
                String counterstr = ei.getCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER);
                boolean serialNumberCleared = false;
                if (StringUtils.isNotEmpty(counterstr)) {
                    try {
                        counter = Integer.valueOf(counterstr);
                        if (log.isDebugEnabled()) {
                            log.debug("Found a counter with value " + counter);
                        }
                        // decrease the counter, if we get to 0 we must set
                        // status to generated
                        counter--;
                        if (counter >= 0) {
                            ei.setCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER, String.valueOf(counter));
                            ei.setCertificateSerialNumber(null);// cert serial number should also be cleared after successful command.
                            data1.setExtendedInformation(ei);
                            serialNumberCleared = true;
                            final Date now = new Date();
                            if (counter > 0) { // if 0 then update when changing type
                                data1.setTimeModified(now.getTime());
                            }
                            String msg = intres.getLocalizedMessage("ra.decreasedentityrequestcounter", username, counter);
                            log.info(msg);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Counter value was already 0, not decreased in db.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        String msg = intres.getLocalizedMessage("ra.errorrequestcounterinvalid", username, counterstr, e.getMessage());
                        log.error(msg, e);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No (optional) request counter exists for end entity: " + username);
                    }
                }
                if (!serialNumberCleared && ei.certificateSerialNumber() != null) {
                    ei.setCertificateSerialNumber(null);// cert serial number should also be cleared after successful command.
                    data1.setExtendedInformation(ei);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No extended information exists for user: " + data1.getUsername());
                }
            }
        } else {
            log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
            // This exception message is used to not leak information to the user
            String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
            log.info(msg);
            throw new FinderException(msg);
        }
        if (counter <= 0) {
            AuthenticationToken admin = new AlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("Local admin call from EndEntityManagementSession.decRequestCounter"));
            try {
                setUserStatus(admin, data1, EndEntityConstants.STATUS_GENERATED);
            } catch (AuthorizationDeniedException e) {
                log.error("Authorization was denied for an AlwaysAllowLocalAuthenticationToken", e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<decRequestCounter(" + username + "): " + counter);
        }
        return counter;
    }

    @Override
    public void cleanUserCertDataSN(EndEntityInformation data) throws ObjectNotFoundException {
        if (log.isTraceEnabled()) {
            log.trace(">cleanUserCertDataSN: " + data.getUsername());
        }
        try {
            cleanUserCertDataSN(data.getUsername());
        } catch (FinderException e) {
            String msg = intres.getLocalizedMessage("authentication.usernotfound", data.getUsername());
            log.info(msg);
            throw new ObjectNotFoundException(e.getMessage());   
        } catch (ApprovalException e) {
            // Should never happen
            log.error("ApprovalException: ", e);
            throw new EJBException(e);
        } catch (WaitingForApprovalException e) {
            // Should never happen
            log.error("WaitingForApprovalException: ", e);
            throw new EJBException(e);
        }
        if (log.isTraceEnabled()) {
            log.trace("<cleanUserCertDataSN: " + data.getUsername());
        }
    }

    @Override
    public void cleanUserCertDataSN(String username) throws FinderException,
            ApprovalException, WaitingForApprovalException {
        if (log.isTraceEnabled()) {
            log.trace(">cleanUserCertDataSN(" + username + ")");
        }
        try {
            // Check if administrator is authorized to edit user.
            UserData data1 = UserData.findByUsername(entityManager, username);
            if (data1 != null) {
                final ExtendedInformation ei = data1.getExtendedInformation();
                if (ei == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No extended information exists for user: " + data1.getUsername());
                    }
                } else {
                    ei.setCertificateSerialNumber(null);
                    data1.setExtendedInformation(ei);
                }
            } else {
                log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
                // This exception message is used to not leak information to the user
                String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
                log.info(msg);
                throw new FinderException(msg);
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("<cleanUserCertDataSN(" + username + ")");
            }
        }
    }

    @Override 
    public long countEndEntitiesUsingCertificateProfile(int certificateprofileid) {
       return UserData.countByCertificateProfileId(entityManager, certificateprofileid);
    }
    
    @Override
    public void setUserStatus(final AuthenticationToken admin, final String username, final int status) throws AuthorizationDeniedException,
            FinderException, ApprovalException, WaitingForApprovalException {
        if (log.isTraceEnabled()) {
            log.trace(">setUserStatus(" + username + ", " + status + ")");
        }
        // Check if administrator is authorized to edit user.
        final UserData data = UserData.findByUsername(entityManager, username);
        if (data == null) {
            log.info(intres.getLocalizedMessage("ra.errorentitynotexist", username));
            // This exception message is used to not leak information to the user
            final String msg = intres.getLocalizedMessage("ra.wrongusernameorpassword");
            log.info(msg);
            throw new FinderException(msg);
        }
        // Check authorization
        final int caid = data.getCaId();
        assertAuthorizedToCA(admin, caid);
        if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
            assertAuthorizedToEndEntityProfile(admin, data.getEndEntityProfileId(), AccessRulesConstants.EDIT_END_ENTITY, caid);
        }
        setUserStatus(admin, data, status);
    }

    private void setUserStatus(final AuthenticationToken admin, final UserData data1, final int status) throws AuthorizationDeniedException,
             ApprovalException, WaitingForApprovalException {
        final int caid = data1.getCaId();
        final String username = data1.getUsername();
        final int endEntityProfileId = data1.getEndEntityProfileId();
        // Check if approvals is required.
        final int numOfApprovalsRequired = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_ADDEDITENDENTITY, caid, data1.getCertificateProfileId());
        if (numOfApprovalsRequired > 0) {
            final ChangeStatusEndEntityApprovalRequest ar = new ChangeStatusEndEntityApprovalRequest(username, data1.getStatus(), status, admin,
                    null, numOfApprovalsRequired, data1.getCaId(), endEntityProfileId);
            if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_SETUSERSTATUS)) {
                approvalSession.addApprovalRequest(admin, ar);
                String msg = intres.getLocalizedMessage("ra.approvaledit");
                throw new WaitingForApprovalException(msg);
            }
        }
        if (data1.getStatus() == EndEntityConstants.STATUS_KEYRECOVERY
                && !(status == EndEntityConstants.STATUS_KEYRECOVERY || status == EndEntityConstants.STATUS_INPROCESS || status == EndEntityConstants.STATUS_INITIALIZED)) {
            keyRecoverySession.unmarkUser(admin, username);
        }
        if ((status == EndEntityConstants.STATUS_NEW) && (data1.getStatus() != EndEntityConstants.STATUS_NEW)) {
            final ExtendedInformation ei = data1.getExtendedInformation();
            if (ei != null) {
                // If status is set to new, when it is not already new, we should
                // re-set the allowed request counter to the default values
                resetRequestCounter(admin, false, ei, username, endEntityProfileId);
                // Reset remaining login counter
                resetRemainingLoginAttemptsInternal(ei, username, caid);
                // data1.setTimeModified(new Date().getTime());
                data1.setExtendedInformation(ei);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Status not changing from something else to new, not resetting requestCounter.");
            }
        }
        final Date timeModified = new Date();
        data1.setStatus(status);
        data1.setTimeModified(timeModified.getTime());
        final String msg = intres.getLocalizedMessage("ra.editedentitystatus", username, Integer.valueOf(status));
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put("msg", msg);
        auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                String.valueOf(caid), null, username, details);
        // Send notifications when transitioning user through work-flow, if they
        // should be sent
        final EndEntityInformation userdata = data1.toEndEntityInformation();
        sendNotification(admin, userdata, status);
        if (log.isTraceEnabled()) {
            log.trace("<setUserStatus(" + username + ", " + status + ")");
        }
    }

    @Override
    public void setPassword(AuthenticationToken admin, String username, String password) throws UserDoesntFullfillEndEntityProfile,
            AuthorizationDeniedException, FinderException {
        setPassword(admin, username, password, false);
    }

    @Override
    public void setClearTextPassword(AuthenticationToken admin, String username, String password) throws UserDoesntFullfillEndEntityProfile,
            AuthorizationDeniedException, FinderException {
        setPassword(admin, username, password, true);
    }

    /**
     * Sets a password, hashed or clear text, for a user.
     * 
     * @param admin the administrator performing the action
     * @param username the unique username.
     * @param password the new password to be stored in clear text. Setting password to 'null' effectively deletes any previous clear text password.
     * @param cleartext true gives cleartext password, false hashed
     */
    private void setPassword(final AuthenticationToken admin, final String username, final String password, final boolean cleartext)
            throws UserDoesntFullfillEndEntityProfile, AuthorizationDeniedException, FinderException {
        if (log.isTraceEnabled()) {
            log.trace(">setPassword(" + username + ", hiddenpwd), " + cleartext);
        }
        // Find user
        String newpasswd = password;
        final UserData data = UserData.findByUsername(entityManager, username);
        if (data == null) {
            throw new FinderException("Could not find user " + username);
        }
        final int caid = data.getCaId();
        final String dn = data.getSubjectDN();
        final int endEntityProfileId = data.getEndEntityProfileId();

        final EndEntityProfile profile = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
        if (profile != null) {
            if (profile.useAutoGeneratedPasswd()) {
                newpasswd = profile.getAutoGeneratedPasswd();
            }
        }
        if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
            // Check if user fulfills it's profile.
            if (profile != null) {
                try {
                    profile.doesPasswordFulfillEndEntityProfile(password, true);
                } catch (UserDoesntFullfillEndEntityProfile ufe) {
                    final String msg = intres.getLocalizedMessage("ra.errorfullfillprofile", Integer.valueOf(endEntityProfileId), dn,
                            ufe.getMessage());
                    Map<String, Object> details = new LinkedHashMap<String, Object>();
                    details.put("msg", msg);
                    auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                            String.valueOf(caid), null, username, details);
                    throw ufe;
                }
            }
            // Check if administrator is authorized to edit user.
            assertAuthorizedToEndEntityProfile(admin, data.getEndEntityProfileId(), AccessRulesConstants.EDIT_END_ENTITY, caid);
        }
        assertAuthorizedToCA(admin, caid);
        try {
            final Date now = new Date();
            if ((newpasswd == null) && (cleartext)) {
                data.setClearPassword("");
                data.setPasswordHash("");
                data.setTimeModified(now.getTime());
            } else {
                if (cleartext) {
                    data.setOpenPassword(newpasswd);
                } else {
                    data.setPassword(newpasswd);
                }
                data.setTimeModified(now.getTime());
            }
            final String msg = intres.getLocalizedMessage("ra.editpwdentity", username);
            Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                    String.valueOf(caid), null, username, details);
        } catch (NoSuchAlgorithmException nsae) {
            log.error("NoSuchAlgorithmException while setting password for user " + username);
            throw new EJBException(nsae);
        }
        if (log.isTraceEnabled()) {
            log.trace("<setPassword(" + username + ", hiddenpwd), " + cleartext);
        }
    }
    
    @Override
    public void updateCAId(final AuthenticationToken admin, final String username, int newCAId)
            throws AuthorizationDeniedException, NoSuchEndEntityException {
        if (log.isTraceEnabled()) {
            log.trace(">updateCAId(" + username + ", "+newCAId+")");
        }
        // Find user
        final UserData data = UserData.findByUsername(entityManager, username);
        if (data == null) {
            throw new NoSuchEndEntityException("Could not find user " + username);
        }
        int oldCAId = data.getCaId();
        assertAuthorizedToCA(admin, oldCAId);
        data.setCaId(newCAId);
        
        final String msg = intres.getLocalizedMessage("ra.updatedentitycaid", username, oldCAId, newCAId);
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put("msg", msg);
        auditSession.log(EjbcaEventTypes.RA_EDITENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
            String.valueOf(oldCAId), null, username, details);
        if (log.isTraceEnabled()) {
            log.trace(">updateCAId(" + username + ", "+newCAId+")");
        }
    }

    @Override
    public boolean verifyPassword(AuthenticationToken admin, String username, String password) throws UserDoesntFullfillEndEntityProfile,
            AuthorizationDeniedException, FinderException {
        if (log.isTraceEnabled()) {
            log.trace(">verifyPassword(" + username + ", hiddenpwd)");
        }
        boolean ret = false;
        // Find user
        final UserData data = UserData.findByUsername(entityManager, username);
        if (data == null) {
            throw new FinderException("Could not find user " + username);
        }
        final int caid = data.getCaId();
        if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
            // Check if administrator is authorized to edit user.
            assertAuthorizedToEndEntityProfile(admin, data.getEndEntityProfileId(), AccessRulesConstants.EDIT_END_ENTITY, caid);
        }
        assertAuthorizedToCA(admin, caid);
        try {
            ret = data.comparePassword(password);
        } catch (NoSuchAlgorithmException nsae) {
            log.debug("NoSuchAlgorithmException while verifying password for user " + username);
            throw new EJBException(nsae);
        }
        if (log.isTraceEnabled()) {
            log.trace("<verifyPassword(" + username + ", hiddenpwd)");
        }
        return ret;
    }

    private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_REVOKEANDDELETEUSER = { new ApprovalOveradableClassName(
            org.ejbca.core.model.approval.approvalrequests.RevocationApprovalRequest.class.getName(), null), };

    @Override
    public void revokeAndDeleteUser(AuthenticationToken admin, String username, int reason) throws AuthorizationDeniedException, ApprovalException,
            WaitingForApprovalException, RemoveException, NotFoundException {
        final UserData data = UserData.findByUsername(entityManager, username);
        if (data == null) {
            throw new NotFoundException("User '" + username + "' not found.");
        }
        // Authorized?
        final int caid = data.getCaId();
        assertAuthorizedToCA(admin, caid);
        if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
            assertAuthorizedToEndEntityProfile(admin, data.getEndEntityProfileId(), AccessRulesConstants.REVOKE_END_ENTITY, caid);
        }
        try {
            if (data.getStatus() != EndEntityConstants.STATUS_REVOKED) {
                // Check if approvals is required.
                final int numOfReqApprovals = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_REVOCATION, caid, data.getCertificateProfileId());
                if (numOfReqApprovals > 0) {
                    final RevocationApprovalRequest ar = new RevocationApprovalRequest(true, username, reason, admin, numOfReqApprovals, caid,
                            data.getEndEntityProfileId());
                    if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_REVOKEANDDELETEUSER)) {
                        approvalSession.addApprovalRequest(admin, ar);
                        throw new WaitingForApprovalException(intres.getLocalizedMessage("ra.approvalrevoke"));
                    }
                }
                try {
                    revokeUser(admin, username, reason);
                } catch (AlreadyRevokedException e) {
                    // This just means that the end entity was revoked before
                    // this request could be completed. No harm.
                }
            }
        } catch (FinderException e) {
            throw new NotFoundException("User " + username + "not found.");
        }
        deleteUser(admin, username);
    }

    private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_REVOKEUSER = {
            new ApprovalOveradableClassName(org.ejbca.core.ejb.ra.EndEntityManagementSessionBean.class.getName(), "revokeAndDeleteUser"),
            new ApprovalOveradableClassName(org.ejbca.core.model.approval.approvalrequests.RevocationApprovalRequest.class.getName(), null), };

    @Override
    public void revokeUser(AuthenticationToken admin, String username, int reason) throws AuthorizationDeniedException, FinderException,
            ApprovalException, WaitingForApprovalException, AlreadyRevokedException {
        if (log.isTraceEnabled()) {
            log.trace(">revokeUser(" + username + ")");
        }
        final UserData userData = UserData.findByUsername(entityManager, username);
        if (userData == null) {
            throw new FinderException("Could not find user " + username);
        }
        final int caid = userData.getCaId();
        assertAuthorizedToCA(admin, caid);
        if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
            assertAuthorizedToEndEntityProfile(admin, userData.getEndEntityProfileId(), AccessRulesConstants.REVOKE_END_ENTITY, caid);
        }

        if ((userData.getStatus() == EndEntityConstants.STATUS_REVOKED) && ((reason == RevokedCertInfo.NOT_REVOKED) || (reason == RevokedCertInfo.REVOCATION_REASON_REMOVEFROMCRL)) ){
            final String msg = intres.getLocalizedMessage("ra.errorinvalidrevokereason", userData.getUsername(), reason);
            log.info(msg);
            throw new AlreadyRevokedException(msg);
        }

        // Check if approvals is required.
        final int numOfReqApprovals = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_REVOCATION, caid, userData.getCertificateProfileId());
        if (numOfReqApprovals > 0) {
            final RevocationApprovalRequest ar = new RevocationApprovalRequest(false, username, reason, admin, numOfReqApprovals, caid,
                    userData.getEndEntityProfileId());
            if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_REVOKEUSER)) {
                approvalSession.addApprovalRequest(admin, ar);
                throw new WaitingForApprovalException(intres.getLocalizedMessage("ra.approvalrevoke"));
            }
        }
        // Revoke all non-expired and not revoked certs, one at the time
        final List<CertificateDataWrapper> cdws = certificateStoreSession.getCertificateDataByUsername(username, true, Arrays.asList(CertificateConstants.CERT_ARCHIVED, CertificateConstants.CERT_REVOKED));
        for (final CertificateDataWrapper cdw : cdws) {
            try {
                final Certificate certificate = cdw.getCertificate();
                final BigInteger serialNumber;
                if (certificate==null) {
                    try {
                        // This will work for X.509
                        serialNumber = new BigInteger(cdw.getCertificateData().getSerialNumber(), 10);
                    } catch (NumberFormatException e) {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    serialNumber = CertTools.getSerialNumber(certificate);
                }
                revokeCert(admin, serialNumber, cdw.getCertificateData().getIssuerDN(), reason);
            } catch (AlreadyRevokedException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Certificate from issuer '" + cdw.getCertificateData().getIssuerDN() + "' with serial " + cdw.getCertificateData().getSerialNumber()
                            + " was already revoked.");
                }
            }
        }
        // Finally set revoke status on the user as well
        try {
            setUserStatus(admin, userData, EndEntityConstants.STATUS_REVOKED);
        } catch (ApprovalException e) {
            throw new EJBException("This should never happen", e);
        } catch (WaitingForApprovalException e) {
            throw new EJBException("This should never happen", e);
        }
        final String msg = intres.getLocalizedMessage("ra.revokedentity", username);
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put("msg", msg);
        auditSession.log(EjbcaEventTypes.RA_REVOKEDENDENTITY, EventStatus.SUCCESS, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(),
                String.valueOf(caid), null, username, details);
        if (log.isTraceEnabled()) {
            log.trace("<revokeUser()");
        }
    }

    private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_REVOKECERT = { new ApprovalOveradableClassName(
            org.ejbca.core.model.approval.approvalrequests.RevocationApprovalRequest.class.getName(), null), };
	private static final String PageRequest = null;

    @Override
    public void revokeCert(final AuthenticationToken admin, final BigInteger certserno, final String issuerdn, final int reason)
            throws AuthorizationDeniedException, FinderException, ApprovalException, WaitingForApprovalException, AlreadyRevokedException {
        try {
            revokeCert(admin, certserno, null, issuerdn, reason, false);
        } catch (RevokeBackDateNotAllowedForProfileException e) {
            throw new IllegalStateException("This should not happen since there is no back dating.",e);
        }
    }

    @Override
    public void revokeCert(AuthenticationToken admin, BigInteger certserno, Date revocationdate, String issuerdn, int reason, boolean checkDate)
            throws AuthorizationDeniedException, FinderException, ApprovalException, WaitingForApprovalException,
            RevokeBackDateNotAllowedForProfileException, AlreadyRevokedException {
     if (log.isTraceEnabled()) {
            log.trace(">revokeCert(" + certserno.toString(16) + ", IssuerDN: " + issuerdn + ")");
        }
        // Check that the admin has revocation rights.
        if (!authorizationSession.isAuthorizedNoLogging(admin, AccessRulesConstants.REGULAR_REVOKEENDENTITY)) {
            String msg = intres.getLocalizedMessage("ra.errorauthrevoke");
            Map<String, Object> details = new LinkedHashMap<String, Object>();
            details.put("msg", msg);
            auditSession.log(EventTypes.ACCESS_CONTROL, EventStatus.FAILURE, EjbcaModuleTypes.RA, ServiceTypes.CORE, admin.toString(), null,
                    certserno.toString(16).toUpperCase(), null, details);
            throw new AuthorizationDeniedException(msg);
        }
        // To be fully backwards compatible we just use the first fingerprint found..
        final CertificateDataWrapper cdw = certificateStoreSession.getCertificateDataByIssuerAndSerno(issuerdn, certserno);
        if (cdw == null) {
            final String msg = intres.getLocalizedMessage("ra.errorfindentitycert", issuerdn, certserno.toString(16));
            log.info(msg);
            throw new FinderException(msg);
        }
        final CertificateData certificateData = cdw.getCertificateData();
        final int caid = certificateData.getIssuerDN().hashCode();
        final String username = certificateData.getUsername();
        assertAuthorizedToCA(admin, caid);
        int certificateProfileId = certificateData.getCertificateProfileId();
        String userDataDN = certificateData.getSubjectDN();
        final CertReqHistory certReqHistory = certreqHistorySession.retrieveCertReqHistory(certserno, issuerdn);
        UserData data = null;
        if (certReqHistory == null) {
            // We could use userdata later, so try to find it
            data = UserData.findByUsername(entityManager, username);
        }
        int endEntityProfileId = -1;
        if (certReqHistory != null) {
            // Get the EEP that was used in the original issuance, if we can find it
            endEntityProfileId = certReqHistory.getEndEntityInformation().getEndEntityProfileId();
            // Republish with the same user DN that was used in the original publication, if we can find it
            userDataDN = certReqHistory.getEndEntityInformation().getCertificateDN();
            // If for some reason the certificate profile id was not set in the certificate data, try to get it from the certreq history
            if (certificateProfileId == CertificateProfileConstants.CERTPROFILE_NO_PROFILE) {
                certificateProfileId = certReqHistory.getEndEntityInformation().getCertificateProfileId();
            }
        } else if (data != null) {
            // Get the EEP that is currently used as a fallback, if we can find it
            endEntityProfileId = data.getEndEntityProfileId();
            // Republish with the same user DN that is currently used as a fallback, if we can find it
            userDataDN = data.toEndEntityInformation().getCertificateDN();
            // If for some reason the certificate profile id was not set in the certificate data, try to get it from current userdata
            if (certificateProfileId == CertificateProfileConstants.CERTPROFILE_NO_PROFILE) {
                certificateProfileId = data.getCertificateProfileId();
            }
        }
        if (endEntityProfileId != -1) {
            // We can only perform this check if we have a trail of what eep was used..
            if (getGlobalConfiguration().getEnableEndEntityProfileLimitations()) {
                assertAuthorizedToEndEntityProfile(admin, endEntityProfileId, AccessRulesConstants.REVOKE_END_ENTITY, caid);
            }
        }
        // Check that unrevocation is not done on anything that can not be unrevoked
        if (reason == RevokedCertInfo.NOT_REVOKED || reason == RevokedCertInfo.REVOCATION_REASON_REMOVEFROMCRL) {
            if (certificateData.getRevocationReason() != RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD) {
                final String msg = intres.getLocalizedMessage("ra.errorunrevokenotonhold", issuerdn, certserno.toString(16));
                log.info(msg);
                throw new AlreadyRevokedException(msg);
            }
        } else {
            if (    certificateData.getRevocationReason()!=RevokedCertInfo.NOT_REVOKED &&
                    // it should be possible to revoke a certificate on hold for good.
                    certificateData.getRevocationReason()!=RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD &&
                    // a valid certificate could have reason "REVOCATION_REASON_REMOVEFROMCRL" if it has been revoked in the past.
                    certificateData.getRevocationReason()!=RevokedCertInfo.REVOCATION_REASON_REMOVEFROMCRL ) {
                final String msg = intres.getLocalizedMessage("ra.errorrevocationexists", issuerdn, certserno.toString(16));
                log.info(msg);
                throw new AlreadyRevokedException(msg);
            }
        }
        if (endEntityProfileId != -1 && certificateProfileId != CertificateProfileConstants.CERTPROFILE_NO_PROFILE) {
            // We can only perform this check if we have a trail of what eep and cp was used..
            // Check if approvals is required.
            final int numOfReqApprovals = getNumOfApprovalRequired(CAInfo.REQ_APPROVAL_REVOCATION, caid, certificateProfileId);
            if (numOfReqApprovals > 0) {
                final RevocationApprovalRequest ar = new RevocationApprovalRequest(certserno, issuerdn, username, reason, admin, numOfReqApprovals,
                        caid, endEntityProfileId);
                if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_REVOKECERT)) {
                    approvalSession.addApprovalRequest(admin, ar);
                    throw new WaitingForApprovalException(intres.getLocalizedMessage("ra.approvalrevoke"));
                }
            }
        }
        // Finally find the publishers for the certificate profileId that we found
        Collection<Integer> publishers = new ArrayList<Integer>(0);
        final CertificateProfile certificateProfile = certificateProfileSession.getCertificateProfile(certificateProfileId);
        if (certificateProfile != null) {
            publishers = certificateProfile.getPublisherList();
            if (publishers == null || publishers.size() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("No publishers defined for certificate with serial #" + certserno.toString(16) + " issued by " + issuerdn);
                }
            }
        } else {
            log.warn("No certificate profile for certificate with serial #" + certserno.toString(16) + " issued by " + issuerdn);
        }
        if ( checkDate && revocationdate!=null && (certificateProfile==null || !certificateProfile.getAllowBackdatedRevocation()) ) {
        	final String profileName = this.certificateProfileSession.getCertificateProfileName(certificateProfileId);
        	final String m = intres.getLocalizedMessage("ra.norevokebackdate", profileName, certserno.toString(16), issuerdn);
        	throw new RevokeBackDateNotAllowedForProfileException(m);
        }
        // Revoke certificate in database and all publishers
        try {
            revocationSession.revokeCertificate(admin, cdw, publishers, revocationdate!=null ? revocationdate : new Date(), reason, userDataDN);
        } catch (CertificateRevokeException e) {
            final String msg = intres.getLocalizedMessage("ra.errorfindentitycert", issuerdn, certserno.toString(16));
            log.info(msg);
            throw new FinderException(msg);
        }
        if (log.isTraceEnabled()) {
            log.trace("<revokeCert()");
        }
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public boolean checkIfCertificateBelongToUser(BigInteger certificatesnr, String issuerdn) {
        if (!WebConfiguration.getRequireAdminCertificateInDatabase()) {
            if (log.isTraceEnabled()) {
                log.trace("<checkIfCertificateBelongToUser Configured to ignore if cert belongs to user.");
            }
            return true;
        }
        final String username = certificateStoreSession.findUsernameByCertSerno(certificatesnr, issuerdn);
        if (username != null) {
            if (UserData.findByUsername(entityManager, username) == null) {
                final String msg = intres.getLocalizedMessage("ra.errorcertnouser", issuerdn, certificatesnr.toString(16));
                log.info(msg);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
       
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Collection<EndEntityInformation> findAllUsersByStatus(AuthenticationToken admin, int status) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersByStatus(" + status + ")");
        }
        if (log.isDebugEnabled()) {
            log.debug("Looking for users with status: " + status);
        }
        Query query = new Query(Query.TYPE_USERQUERY);
        query.add(UserMatch.MATCH_WITH_STATUS, BasicMatch.MATCH_TYPE_EQUALS, Integer.toString(status));
        Collection<EndEntityInformation> returnval = null;
        try {
            returnval = query(admin, query, null, null, 0, AccessRulesConstants.VIEW_END_ENTITY);
        } catch (IllegalQueryException e) {
        }
        if (log.isDebugEnabled()) {
            log.debug("found " + returnval.size() + " user(s) with status=" + status);
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersByStatus(" + status + ")");
        }
        return returnval;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Collection<EndEntityInformation> findAllUsersByCaId(AuthenticationToken admin, int caid) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersByCaId(" + caid + ")");
        }
        if (log.isDebugEnabled()) {
            log.debug("Looking for users with caid: " + caid);
        }
        Query query = new Query(Query.TYPE_USERQUERY);
        query.add(UserMatch.MATCH_WITH_CA, BasicMatch.MATCH_TYPE_EQUALS, Integer.toString(caid));
        Collection<EndEntityInformation> returnval = null;
        try {
            returnval = query(admin, query, null, null, 0, AccessRulesConstants.VIEW_END_ENTITY);
        } catch (IllegalQueryException e) {
            // Ignore ??
            log.debug("Illegal query", e);
            returnval = new ArrayList<EndEntityInformation>();
        }
        if (log.isDebugEnabled()) {
            log.debug("found " + returnval.size() + " user(s) with caid=" + caid);
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersByCaId(" + caid + ")");
        }
        return returnval;
    }

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<EndEntityInformation> findUsers(List<Integer> caIds, long timeModified, int status) {
        String queryString = "SELECT a FROM UserData a WHERE (a.timeModified <=:timeModified) AND (a.status=:status)";
        if (caIds.size() > 0) {
            queryString += " AND (a.caId=:caId0";
            for (int i = 1; i < caIds.size(); i++) {
                queryString += " OR a.caId=:caId" + i;
            }
            queryString += ")";
        }
        if (log.isDebugEnabled()) {
            log.debug("Checking for " + caIds.size() + " CAs");
            log.debug("Generated query string: " + queryString);
        }
        javax.persistence.Query query = entityManager.createQuery(queryString);
        query.setParameter("timeModified", timeModified);
        query.setParameter("status", status);
        if (caIds.size() > 0) {
            for (int i = 0; i < caIds.size(); i++) {
                query.setParameter("caId" + i, caIds.get(i));
            }
        }
        final List<UserData> queryResult = (List<UserData>) query.getResultList();
        final List<EndEntityInformation> ret = new ArrayList<EndEntityInformation>(queryResult.size());
        for (UserData userData : queryResult) {
            ret.add(userData.toEndEntityInformation());
        }
        return ret;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Collection<EndEntityInformation> findAllUsersWithLimit(AuthenticationToken admin) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersWithLimit()");
        }
        Collection<EndEntityInformation> returnval = null;
        try {
            returnval = query(admin, null, null, null, 0, AccessRulesConstants.VIEW_END_ENTITY);
        } catch (IllegalQueryException e) {
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersWithLimit()");
        }
        return returnval;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<EndEntityInformation> findAllBatchUsersByStatusWithLimit(int status) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersByStatusWithLimit()");
        }
        final javax.persistence.Query query = entityManager.createQuery("SELECT a FROM UserData a WHERE a.status=:status AND (clearPassword IS NOT NULL)");
        query.setParameter("status", status);
        query.setMaxResults(getGlobalCesecoreConfiguration().getMaximumQueryCount());        
        @SuppressWarnings("unchecked")
        final List<UserData> userDataList = query.getResultList();
        final List<EndEntityInformation> returnval = new ArrayList<EndEntityInformation>(userDataList.size());
        for (UserData ud : userDataList) {
            EndEntityInformation endEntityInformation = ud.toEndEntityInformation();
            if (endEntityInformation.getPassword() != null && endEntityInformation.getPassword().length() > 0) {
                returnval.add(endEntityInformation);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersByStatusWithLimit()");
        }
        return returnval;
    }
    
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<EndEntityInformation> findAllUsersByCaIdNoAuth(int caid) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersByCaIdNoAuth()");
        }
        final List<UserData> userDataList = UserData.findByCAId(entityManager, caid);
        final List<EndEntityInformation> returnval = new ArrayList<EndEntityInformation>(userDataList.size());
        for (UserData ud : userDataList) {
            returnval.add(ud.toEndEntityInformation());
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersByCaIdNoAuth()");
        }
        return returnval;
    }

    /**
     * 
     * Help function used to retrieve user information. A query parameter of null indicates all users. If caauthorizationstring or
     * endentityprofilestring are null then the method will retrieve the information itself.
     * 
     * 
     * @param admin
     * @param query
     * @param withlimit
     * @param caauthorizationstr
     * @param endentityprofilestr
     * @param numberofrows the number of rows to fetch, use 0 for the value defined in GlobalConfiguration 
     * @param endentityAccessRule
     * @return
     * @throws IllegalQueryException
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Collection<EndEntityInformation> query(final AuthenticationToken admin, final Query query, final String caauthorizationstr,
            final String endentityprofilestr, final int numberofrows, final String endentityAccessRule) throws IllegalQueryException {
        boolean authorizedtoanyprofile = true;
        final String caauthorizationstring = StringTools.strip(caauthorizationstr);
        final String endentityprofilestring = StringTools.strip(endentityprofilestr);
        final ArrayList<EndEntityInformation> returnval = new ArrayList<EndEntityInformation>();
        int fetchsize = getGlobalCesecoreConfiguration().getMaximumQueryCount();
         if (numberofrows != 0) {
            fetchsize = numberofrows;
        }

        // Check if query is legal.
        if (query != null && !query.isLegalQuery()) {
            throw new IllegalQueryException();
        }

        String sqlquery = "";
        if (query != null) {
            sqlquery = sqlquery + query.getQueryString();
        }

        final GlobalConfiguration globalconfiguration = getGlobalConfiguration();
        String caauthstring = caauthorizationstring;
        String endentityauth = endentityprofilestring;
        BciaRAAuthorization raauthorization = null;
        if (caauthorizationstring == null || endentityprofilestring == null) {
            raauthorization = new BciaRAAuthorization(admin, globalConfigurationSession, authorizationSession, complexAccessControlSession, caSession,
                    endEntityProfileSession);
            caauthstring = raauthorization.getCAAuthorizationString();
            if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
                endentityauth = raauthorization.getEndEntityProfileAuthorizationString(true, endentityAccessRule);
            } else {
                endentityauth = "";
            }
        }

        if (!caauthstring.trim().equals("") && query != null) {
            sqlquery = sqlquery + " AND " + caauthstring;
        } else {
            sqlquery = sqlquery + caauthstring;
        }

        if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
            if (endentityauth == null || endentityauth.trim().equals("")) {
                authorizedtoanyprofile = false;
            } else {
                if (caauthstring.trim().equals("") && query == null) {
                    sqlquery = sqlquery + endentityauth;
                } else {
                    sqlquery = sqlquery + " AND " + endentityauth;
                }
            }
        }
        // Finally order the return values
        sqlquery += " ORDER BY " + USERDATA_CREATED_COL + " DESC";
        if (log.isDebugEnabled()) {
            log.debug("generated query: " + sqlquery);
        }
        if (authorizedtoanyprofile) {
            final javax.persistence.Query dbQuery = entityManager.createQuery("SELECT a FROM UserData a WHERE " + sqlquery);
            if (fetchsize > 0) {
                dbQuery.setMaxResults(fetchsize);
            }           
            @SuppressWarnings("unchecked")
            final List<UserData> userDataList = dbQuery.getResultList();
            for (UserData userData : userDataList) {
                returnval.add(userData.toEndEntityInformation());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("authorizedtoanyprofile=false");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<query(): " + returnval.size());
        }
        return returnval;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public boolean checkForEndEntityProfileId(int endentityprofileid) {
        if (log.isTraceEnabled()) {
            log.trace(">checkForEndEntityProfileId(" + endentityprofileid + ")");
        }
        long count = UserData.countByEndEntityProfileId(entityManager, endentityprofileid);
        if (log.isTraceEnabled()) {
            log.trace("<checkForEndEntityProfileId(" + endentityprofileid + "): " + count);
        }
        return count > 0;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<String> findByCertificateProfileId(int certificateprofileid) {
        if (log.isTraceEnabled()) {
            log.trace(">checkForCertificateProfileId("+certificateprofileid+")");
        }
        final javax.persistence.Query query = entityManager.createQuery("SELECT a FROM UserData a WHERE a.certificateProfileId=:certificateProfileId");
        query.setParameter("certificateProfileId", certificateprofileid);

        List<String> result = new ArrayList<String>();
        for(Object userDataObject : query.getResultList()) {
                result.add(((UserData) userDataObject).getUsername());
        }
        if (log.isTraceEnabled()) {
            log.trace("<checkForCertificateProfileId("+certificateprofileid+"): "+result.size());
        }
        return result;
        
    }
   
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public boolean checkForCAId(int caid) {
        if (log.isTraceEnabled()) {
            log.trace(">checkForCAId()");
        }
        return UserData.countByCaId(entityManager, caid) > 0;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public boolean checkForHardTokenProfileId(int profileid) {
        if (log.isTraceEnabled()) {
            log.trace(">checkForHardTokenProfileId()");
        }
        return UserData.countByHardTokenProfileId(entityManager, profileid) > 0;
    }

    private void print(EndEntityProfile profile, EndEntityInformation userdata) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("profile.getUsePrinting(): "+profile.getUsePrinting());
            }
            if (profile.getUsePrinting()) {
                String[] pINs = new String[1];
                pINs[0] = userdata.getPassword();
                PrinterManager.print(profile.getPrinterName(), profile.getPrinterSVGFileName(), profile.getPrinterSVGData(),
                        profile.getPrintedCopies(), 0, userdata, pINs, new String[0], "", "", "");
            }
        } catch (PrinterException e) {
            String msg = intres.getLocalizedMessage("ra.errorprint", userdata.getUsername(), e.getMessage());
            log.error(msg, e);
        }
    }

    private void sendNotification(final AuthenticationToken admin, final EndEntityInformation data, final int newstatus) {
        if (data == null) {
            if (log.isDebugEnabled()) {
                log.debug("No UserData, no notification sent.");
            }
            return;
        }
        final String useremail = data.getEmail();
        if (log.isTraceEnabled()) {
            log.trace(">sendNotification: user=" + data.getUsername() + ", email=" + useremail);
        }

        // Make check if we should send notifications at all
        if (data.getType().contains(EndEntityTypes.SENDNOTIFICATION)) {
            final int profileId = data.getEndEntityProfileId();
            final EndEntityProfile profile = endEntityProfileSession.getEndEntityProfileNoClone(profileId);
            final Collection<UserNotification> l = profile.getUserNotifications();
            if (log.isDebugEnabled()) {
                log.debug("Number of user notifications: " + l.size());
            }
            final Iterator<UserNotification> i = l.iterator();
            String rcptemail = useremail; // Default value
            while (i.hasNext()) {
                final UserNotification not = i.next();
                final Collection<String> events = not.getNotificationEventsCollection();
                if (events.contains(String.valueOf(newstatus))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Status is " + newstatus + ", notification sent for notificationevents: " + not.getNotificationEvents());
                    }
                    try {
                        if (StringUtils.equals(not.getNotificationRecipient(), UserNotification.RCPT_USER)) {
                            rcptemail = useremail;
                        } else if (StringUtils.contains(not.getNotificationRecipient(), UserNotification.RCPT_CUSTOM)) {
                            rcptemail = "custom"; // Just if this fail it will
                                                  // say that sending to user
                                                  // with email "custom" failed.
                            // Plug-in mechanism for retrieving custom
                            // notification email recipient addresses
                            if (not.getNotificationRecipient().length() < 6) {
                                final String msg = intres.getLocalizedMessage("ra.errorcustomrcptshort", not.getNotificationRecipient());
                                log.error(msg);
                            } else {
                                final String cp = not.getNotificationRecipient().substring(7);
                                if (StringUtils.isNotEmpty(cp)) {
                                    final ICustomNotificationRecipient plugin = (ICustomNotificationRecipient) Thread.currentThread()
                                            .getContextClassLoader().loadClass(cp).newInstance();
                                    rcptemail = plugin.getRecipientEmails(data);
                                    if (StringUtils.isEmpty(rcptemail)) {
                                        final String msg = intres.getLocalizedMessage("ra.errorcustomnoemail", not.getNotificationRecipient());
                                        log.error(msg);
                                    } else {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Custom notification recipient plugin returned email: " + rcptemail);
                                        }
                                    }
                                } else {
                                    final String msg = intres.getLocalizedMessage("ra.errorcustomnoclasspath", not.getNotificationRecipient());
                                    log.error(msg);
                                }
                            }
                        } else {
                            // Just a plain email address specified in the
                            // recipient field
                            rcptemail = not.getNotificationRecipient();
                        }
                        if (StringUtils.isEmpty(rcptemail)) {
                            final String msg = intres.getLocalizedMessage("ra.errornotificationnoemail", data.getUsername());
                            throw new Exception(msg);
                        }
                        // Get the administrators DN from the admin certificate, if one exists
                        // When approvals is used, this will be the DN of the admin that approves the request
                        String approvalAdminDN = null;
                        EndEntityInformation approvalAdmin = null;
                        if (admin instanceof X509CertificateAuthenticationToken) {
                            final X509CertificateAuthenticationToken xtok = (X509CertificateAuthenticationToken) admin;
                            final X509Certificate adminCert = xtok.getCertificate();
                            String username = certificateStoreSession.findUsernameByIssuerDnAndSerialNumber(CertTools.getIssuerDN(adminCert),
                                    adminCert.getSerialNumber());
                            approvalAdmin = endEntityAccessSession.findUser(new AlwaysAllowLocalAuthenticationToken(new UsernamePrincipal(
                                    "EndEntityManagementSession")), username);
                        }
                        final UserNotificationParamGen paramGen = new UserNotificationParamGen(data, approvalAdminDN, approvalAdmin);
                        /*
                         * substitute any $ fields in the receipient and from
                         * fields
                         */
                        rcptemail = paramGen.interpolate(rcptemail);
                        final String fromemail = paramGen.interpolate(not.getNotificationSender());
                        final String subject = paramGen.interpolate(not.getNotificationSubject());
                        final String message = paramGen.interpolate(not.getNotificationMessage());
                        MailSender.sendMailOrThrow(fromemail, Arrays.asList(rcptemail), MailSender.NO_CC, subject, message, MailSender.NO_ATTACHMENTS);
                        final String logmsg = intres.getLocalizedMessage("ra.sentnotification", data.getUsername(), rcptemail);
                        log.info(logmsg);
                    } catch (Exception e) {
                        final String msg = intres.getLocalizedMessage("ra.errorsendnotification", data.getUsername(), rcptemail);
                        log.error(msg, e);
                    }
                } else { // if (events.contains(String.valueOf(newstatus)))
                    if (log.isDebugEnabled()) {
                        log.debug("Status is " + newstatus + ", no notification sent for notificationevents: " + not.getNotificationEvents());
                    }
                }
            }
        } else { // if ( ((data.getType() & EndEntityTypes.USER_SENDNOTIFICATION) != 0) )
            if (log.isDebugEnabled()) {
                log.debug("Type ("+data.getType().getHexValue()+") does not contain EndEntityTypes.USER_SENDNOTIFICATION, no notification sent.");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<sendNotification: user=" + data.getUsername() + ", email=" + useremail);
        }
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public boolean existsUser(String username) {
        boolean returnval = true;
        if (UserData.findByUsername(entityManager, username) == null) {
            returnval = false;
        }
        return returnval;
    }

    @Override
    public boolean prepareForKeyRecovery(AuthenticationToken admin, String username, int endEntityProfileId, Certificate certificate)
            throws AuthorizationDeniedException, ApprovalException, WaitingForApprovalException {
        boolean ret;
        if (certificate == null) {
            ret = keyRecoverySession.markNewestAsRecoverable(admin, username, endEntityProfileId);
        } else {
            ret = keyRecoverySession.markAsRecoverable(admin, certificate, endEntityProfileId);
        }
        try {
            setUserStatus(admin, username, EndEntityConstants.STATUS_KEYRECOVERY);
        } catch (FinderException e) {
            ret = false;
            log.info("prepareForKeyRecovery: No such user: " + username);
        }
        return ret;
    }

    //
    // Private helper methods
    //
    /**
     * re-sets the optional request counter of a user to the default value specified by the end entity profile. If the profile does not specify that
     * request counter should be used, the counter is removed.
     * 
     * @param admin administrator
     * @param ei the ExtendedInformation object to modify
     */
    private void resetRequestCounter(final AuthenticationToken admin, final boolean onlyRemoveNoUpdate, final ExtendedInformation ei,
            final String username, final int endEntityProfileId) {
        if (log.isTraceEnabled()) {
            log.trace(">resetRequestCounter(" + username + ", " + onlyRemoveNoUpdate + ")");
        }
        final EndEntityProfile prof = endEntityProfileSession.getEndEntityProfileNoClone(endEntityProfileId);
        String value = null;
        if (prof != null) {
            if (prof.getUse(EndEntityProfile.ALLOWEDREQUESTS, 0)) {
                value = prof.getValue(EndEntityProfile.ALLOWEDREQUESTS, 0);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Can not fetch entity profile with id " + endEntityProfileId);
            }
        }
        final String counter = ei.getCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER);
        if (log.isDebugEnabled()) {
            log.debug("Old counter is: " + counter + ", new counter will be: " + value);
        }
        // If this end entity profile does not use ALLOWEDREQUESTS, this
        // value will be set to null
        // We only re-set this value if the COUNTER was used in the first
        // place, if never used, we will not fiddle with it
        if (counter != null) {
            if ((!onlyRemoveNoUpdate) || (onlyRemoveNoUpdate && (value == null))) {
                ei.setCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER, value);
                if (log.isDebugEnabled()) {
                    log.debug("Re-set request counter for user '" + username + "' to:" + value);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No re-setting counter because we should only remove");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Request counter not used, not re-setting it.");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<resetRequestCounter(" + username + ", " + onlyRemoveNoUpdate + ")");
        }
    }

	@Override
	public Collection<EndEntityInformation> findAllUsers(AuthenticationToken admin,int currentPage,int rowsPerPage) {
        if (log.isTraceEnabled()) {
            log.trace(">findAllUsersWithLimit()");
        }
        Collection<EndEntityInformation> returnval = null;
        try {
            returnval = queryPage(admin, null, null, null, currentPage, rowsPerPage,  AccessRulesConstants.VIEW_END_ENTITY);
        } catch (IllegalQueryException e) {
        }
        if (log.isTraceEnabled()) {
            log.trace("<findAllUsersWithLimit()");
        }
        return returnval;
    }
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Collection<EndEntityInformation> queryPage(final AuthenticationToken admin, final Query query, final String caauthorizationstr,
            final String endentityprofilestr,int currentPage,int rowsPerPage,  final String endentityAccessRule) throws IllegalQueryException {

        boolean authorizedtoanyprofile = true;
        final String caauthorizationstring = StringTools.strip(caauthorizationstr);
        final String endentityprofilestring = StringTools.strip(endentityprofilestr);
        final ArrayList<EndEntityInformation> returnval = new ArrayList<EndEntityInformation>();

        // Check if query is legal.
        if (query != null && !query.isLegalQuery()) {
            throw new IllegalQueryException();
        }

        String sqlquery = "";
        if (query != null) {
            sqlquery = sqlquery + query.getQueryString();
        }

        final GlobalConfiguration globalconfiguration = getGlobalConfiguration();
        String caauthstring = caauthorizationstring;
        String endentityauth = endentityprofilestring;
        BciaRAAuthorization raauthorization = null;
        if (caauthorizationstring == null || endentityprofilestring == null) {
            raauthorization = new BciaRAAuthorization(admin, globalConfigurationSession, authorizationSession, complexAccessControlSession, caSession,
                    endEntityProfileSession);
            caauthstring = raauthorization.getCAAuthorizationString();
            if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
                endentityauth = raauthorization.getEndEntityProfileAuthorizationString(true, endentityAccessRule);
            } else {
                endentityauth = "";
            }
        }

        if (!caauthstring.trim().equals("") && query != null) {
            sqlquery = sqlquery + " AND " + caauthstring;
        } else {
            sqlquery = sqlquery + caauthstring;
        }

        if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
            if (endentityauth == null || endentityauth.trim().equals("")) {
                authorizedtoanyprofile = false;
            } else {
                if (caauthstring.trim().equals("") && query == null) {
                    sqlquery = sqlquery + endentityauth;
                } else {
                    sqlquery = sqlquery + " AND " + endentityauth;
                }
            }
        }
        // Finally order the return values
        sqlquery += " ORDER BY " + USERDATA_CREATED_COL + " DESC";
        if (log.isDebugEnabled()) {
            log.debug("generated query: " + sqlquery);
        }
        if (authorizedtoanyprofile) {
            final javax.persistence.Query dbQuery = entityManager.createQuery("SELECT a FROM UserData a WHERE " + sqlquery);
            int firstIndex=(currentPage-1)*rowsPerPage;
            dbQuery.setFirstResult(firstIndex);
            dbQuery.setMaxResults(rowsPerPage);
            @SuppressWarnings("unchecked")
            final List<UserData> userDataList = dbQuery.getResultList();
            for (UserData userData : userDataList) {
                returnval.add(userData.toEndEntityInformation());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("authorizedtoanyprofile=false");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<query(): " + returnval.size());
        }
        return returnval;
    
		
	}

	@Override
	public int findAllUsersTotal(AuthenticationToken admin) throws IllegalQueryException {
 		return getTotal(admin, null, null, null,  AccessRulesConstants.VIEW_END_ENTITY);
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int getTotal(final AuthenticationToken admin, final Query query, final String caauthorizationstr,
            final String endentityprofilestr,  final String endentityAccessRule) throws IllegalQueryException {
        boolean authorizedtoanyprofile = true;
        final String caauthorizationstring = StringTools.strip(caauthorizationstr);
        final String endentityprofilestring = StringTools.strip(endentityprofilestr);
        final ArrayList<EndEntityInformation> returnval = new ArrayList<EndEntityInformation>();
         // Check if query is legal.
        if (query != null && !query.isLegalQuery()) {
            throw new IllegalQueryException();
        }

        String sqlquery = "";
        if (query != null) {
            sqlquery = sqlquery + query.getQueryString();
        }

        final GlobalConfiguration globalconfiguration = getGlobalConfiguration();
        String caauthstring = caauthorizationstring;
        String endentityauth = endentityprofilestring;
        BciaRAAuthorization raauthorization = null;
        if (caauthorizationstring == null || endentityprofilestring == null) {
            raauthorization = new BciaRAAuthorization(admin, globalConfigurationSession, authorizationSession, complexAccessControlSession, caSession,
                    endEntityProfileSession);
            caauthstring = raauthorization.getCAAuthorizationString();
            if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
                endentityauth = raauthorization.getEndEntityProfileAuthorizationString(true, endentityAccessRule);
            } else {
                endentityauth = "";
            }
        }

        if (!caauthstring.trim().equals("") && query != null) {
            sqlquery = sqlquery + " AND " + caauthstring;
        } else {
            sqlquery = sqlquery + caauthstring;
        }

        if (globalconfiguration.getEnableEndEntityProfileLimitations()) {
            if (endentityauth == null || endentityauth.trim().equals("")) {
                authorizedtoanyprofile = false;
            } else {
                if (caauthstring.trim().equals("") && query == null) {
                    sqlquery = sqlquery + endentityauth;
                } else {
                    sqlquery = sqlquery + " AND " + endentityauth;
                }
            }
        }
        // Finally order the return values
        sqlquery += " ORDER BY " + USERDATA_CREATED_COL + " DESC";
        if (log.isDebugEnabled()) {
            log.debug("generated query: " + sqlquery);
        }
        if (authorizedtoanyprofile) {
            final javax.persistence.Query dbQuery = entityManager.createQuery("SELECT count(*) FROM UserData a WHERE " + sqlquery);
            Object  res = dbQuery.getSingleResult();
            return Integer.parseInt(res.toString());
         } else {
            if (log.isDebugEnabled()) {
                log.debug("authorizedtoanyprofile=false");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("<query(): " + returnval.size());
        }
        return 0;
    }

}
