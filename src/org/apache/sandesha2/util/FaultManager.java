/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.sandesha2.util;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.FaultData;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SandeshaListener;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beans.RMDBean;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Has logic to check for possible RM related faults and create it.
 */

public class FaultManager {

	private static final Log log = LogFactory.getLog(FaultManager.class);

	/**
	 * Check weather the LastMessage number has been exceeded and generate the
	 * fault if it is.
	 * 
	 * @param msgCtx
	 * @return
	 */
	public static void checkForLastMsgNumberExceeded(RMMsgContext applicationRMMessage, StorageManager storageManager)
			throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: FaultManager::checkForLastMsgNumberExceeded");
/*	
 * TODO - This code currently doesn't actually work	
		Sequence sequence = (Sequence) applicationRMMessage.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		long messageNumber = sequence.getMessageNumber().getMessageNumber();
		String sequenceID = sequence.getIdentifier().getIdentifier();

		boolean lastMessageNumberExceeded = false;
		String reason = null;
		
		RMSBean rmsBean = SandeshaUtil.getRMSBeanFromSequenceId(storageManager, sequenceID);
		if (rmsBean != null) {
			long lastMessageNo = rmsBean.getLastOutMessage();
			if (messageNumber > lastMessageNo) {
				lastMessageNumberExceeded = true;
				reason = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.msgNumberLargerThanLastMsg, Long
						.toString(messageNumber), Long.toString(lastMessageNo));
			}
		}

		if (lastMessageNumberExceeded) {
			FaultData faultData = new FaultData();
			faultData.setType(Sandesha2Constants.SOAPFaults.FaultType.LAST_MESSAGE_NO_EXCEEDED);
			int SOAPVersion = SandeshaUtil.getSOAPVersion(applicationRMMessage.getSOAPEnvelope());
			if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
				faultData.setCode(SOAP11Constants.FAULT_CODE_SENDER);
			else
				faultData.setCode(SOAP12Constants.FAULT_CODE_SENDER);

			faultData.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.LAST_MESSAGE_NO_EXCEEDED);
			faultData.setReason(reason);
			
			SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);
			String rmNamespace = applicationRMMessage.getRMNamespaceValue();
			OMElement identifierElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.IDENTIFIER,
					rmNamespace, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
			identifierElement.setText(sequenceID);
			
			faultData.setDetail(identifierElement);
			
			if (log.isDebugEnabled())
				log.debug("Exit: FaultManager::checkForLastMsgNumberExceeded, lastMessageNumberExceeded");
			getFault(applicationRMMessage, faultData, storageManager);
		}
*/
		if (log.isDebugEnabled())
			log.debug("Exit: FaultManager::checkForLastMsgNumberExceeded");
	}

	public static RMMsgContext checkForMessageNumberRoleover(MessageContext messageContext) {
		return null;
	}

	/**
	 * Check whether a Sequence message (a) belongs to a unknown sequence
	 * (generates an UnknownSequence fault) (b) message number exceeds a
	 * predifined limit ( genenrates a Message Number Rollover fault)
	 * 
	 * @param msgCtx
	 * @return
	 * @throws SandeshaException
	 */
	public static void checkForUnknownSequence(RMMsgContext rmMessageContext, String sequenceID,
			StorageManager storageManager) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: FaultManager::checkForUnknownSequence, " + sequenceID);

		boolean validSequence = false;

		// Look for an outbound sequence
		if (SandeshaUtil.getRMSBeanFromSequenceId(storageManager, sequenceID) != null) {
			validSequence = true;
			// Look for an inbound sequence
		} else if(SandeshaUtil.getRMDBeanFromSequenceId(storageManager, sequenceID) != null) { 
				validSequence = true;
		}

		if (!validSequence) {

			if (log.isDebugEnabled())
				log.debug("Sequence not valid " + sequenceID);

			// Return an UnknownSequence error
			MessageContext messageContext = rmMessageContext.getMessageContext();

			int SOAPVersion = SandeshaUtil.getSOAPVersion(messageContext.getEnvelope());

			FaultData data = new FaultData();
			if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
				data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
			else
				data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

			data.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.UNKNOWN_SEQUENCE);

			SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);

			OMElement identifierElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.IDENTIFIER,
					rmMessageContext.getRMNamespaceValue(), Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
			identifierElement.setText(sequenceID);
			
			data.setDetail(identifierElement);

			data.setReason(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.noSequenceEstablished, sequenceID));

			if (log.isDebugEnabled())
				log.debug("Exit: FaultManager::checkForUnknownSequence, Sequence unknown");
			getFault(rmMessageContext, data);
		}

		if (log.isDebugEnabled())
			log.debug("Exit: FaultManager::checkForUnknownSequence");
	}

	/**
	 * Check weather the Acknowledgement is invalid and generate a fault if it
	 * is.
	 * 
	 * @param msgCtx
	 * @return
	 * @throws SandeshaException
	 */
	public static void checkForInvalidAcknowledgement(RMMsgContext ackRMMessageContext, StorageManager storageManager)
			throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: FaultManager::checkForInvalidAcknowledgement");

		// check lower<=upper
		// TODO acked for not-send message

		MessageContext ackMessageContext = ackRMMessageContext.getMessageContext();
		if (ackRMMessageContext.getMessageType() != Sandesha2Constants.MessageTypes.ACK) {
			if (log.isDebugEnabled())
				log.debug("Exit: FaultManager::checkForInvalidAcknowledgement, MessageType not an ACK");
		}

		boolean invalidAck = false;
		String reason = null;
		
		Iterator sequenceAckIter = ackRMMessageContext.getMessageParts(
				Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		
		while (sequenceAckIter.hasNext()) {
			SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) sequenceAckIter.next();
			List sequenceAckList = sequenceAcknowledgement.getAcknowledgementRanges();
			Iterator it = sequenceAckList.iterator();

			while (it.hasNext()) {
				AcknowledgementRange acknowledgementRange = (AcknowledgementRange) it.next();
				long upper = acknowledgementRange.getUpperValue();
				long lower = acknowledgementRange.getLowerValue();

				if (lower > upper) {
					invalidAck = true;
					reason = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.ackInvalid, Long.toString(lower), Long
							.toString(upper));
				}
			}

			if (invalidAck) {
				FaultData data = new FaultData();
				int SOAPVersion = SandeshaUtil.getSOAPVersion(ackMessageContext.getEnvelope());
				if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
					data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
				else
					data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

				data.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.INVALID_ACKNOWLEDGEMENT);
				data.setReason(reason);

				SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);
				OMElement dummyElement = factory.createOMElement("dummyElem", null);
				sequenceAcknowledgement.toOMElement(dummyElement);

				OMElement sequenceAckElement = dummyElement.getFirstChildWithName(new QName(
						Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK));
				data.setDetail(sequenceAckElement);

				if (log.isDebugEnabled())
					log.debug("Exit: FaultManager::checkForInvalidAcknowledgement, invalid ACK");
				getFault(ackRMMessageContext, data);
			}
		
		}

		if (log.isDebugEnabled())
			log.debug("Exit: FaultManager::checkForInvalidAcknowledgement");
	}

	public static void checkForSequenceClosed(RMMsgContext referenceRMMessage, String sequenceID,
			RMDBean rmdBean) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: FaultManager::checkForSequenceClosed, " + sequenceID);

		MessageContext referenceMessage = referenceRMMessage.getMessageContext();

		boolean sequenceClosed = false;
		String reason = null;
		if (rmdBean.isClosed()) {
			sequenceClosed = true;
			reason = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotAcceptMsgAsSequenceClosed, sequenceID);
		}

		if (sequenceClosed) {
			FaultData data = new FaultData();
			int SOAPVersion = SandeshaUtil.getSOAPVersion(referenceMessage.getEnvelope());
			if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
				data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
			else
				data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

			data.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.SEQUENCE_CLOSED);
			data.setReason(reason);
			
			SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);
			String rmNamespaceValue = referenceRMMessage.getRMNamespaceValue();
			OMElement identifierElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.IDENTIFIER,
					rmNamespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
			identifierElement.setText(sequenceID);
			
			data.setDetail(identifierElement);

			if (log.isDebugEnabled())
				log.debug("Exit: FaultManager::checkForSequenceClosed, sequence closed");
			getFault(referenceRMMessage, data);
		}

		if (log.isDebugEnabled())
			log.debug("Exit: FaultManager::checkForSequenceClosed");
	}
	
	/**
	 * Adds the necessary Fault elements as properties to the message context.
	 * Returns a dummy Fault which will be throw by this method caller.
	 * 
	 * @param referenceRMMsgContext - Message in reference to which the fault will be generated.
	 * @param data - data for the fault
	 * @return - The dummy fault to be thrown out.
	 * 
	 * @throws AxisFault
	 */
	public static void getFault (RMMsgContext referenceRMMsgContext, FaultData data) throws AxisFault {
		
		SOAPFactory factory = (SOAPFactory) referenceRMMsgContext.getSOAPEnvelope().getOMFactory();
		
		SOAPFaultCode faultCode = factory.createSOAPFaultCode();
		SOAPFaultSubCode faultSubCode = factory.createSOAPFaultSubCode(faultCode);
		
		SOAPFaultValue faultColdValue = factory.createSOAPFaultValue(faultCode);
		SOAPFaultValue faultSubcodeValue = factory.createSOAPFaultValue(faultSubCode);
		
		faultColdValue.setText(data.getCode());
		faultSubcodeValue.setText(data.getSubcode());

		faultCode.setSubCode(faultSubCode);
		
		SOAPFaultReason reason = factory.createSOAPFaultReason();
		SOAPFaultText reasonText = factory.createSOAPFaultText();
		reasonText.setText(data.getReason());
		reason.addSOAPText(reasonText);
		
		SOAPFaultDetail detail = factory.createSOAPFaultDetail();
		detail.addDetailEntry(data.getDetail());
		
		String SOAPNamespaceValue = factory.getSoapVersionURI();
		
		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPNamespaceValue)) {
			referenceRMMsgContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, faultCode);
			referenceRMMsgContext.setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, reason);
			referenceRMMsgContext.setProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, detail);
		} else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals (SOAPNamespaceValue)) {
			referenceRMMsgContext.setProperty(SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, faultCode);
			referenceRMMsgContext.setProperty(SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, detail);
		} else {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.unknownSoapVersion);
			throw new SandeshaException (message);
		}
		
		SandeshaException fault = new SandeshaException("");
		throw fault;
	}
	
	public static boolean isRMFault (String faultSubcodeValue) {
		if (faultSubcodeValue==null)
			return false;
		
		if (Sandesha2Constants.SOAPFaults.Subcodes.CREATE_SEQUENCE_REFUSED.equalsIgnoreCase (faultSubcodeValue) ||
			Sandesha2Constants.SOAPFaults.Subcodes.INVALID_ACKNOWLEDGEMENT.equalsIgnoreCase (faultSubcodeValue) ||	
			Sandesha2Constants.SOAPFaults.Subcodes.LAST_MESSAGE_NO_EXCEEDED.equalsIgnoreCase (faultSubcodeValue) ||
			Sandesha2Constants.SOAPFaults.Subcodes.MESSAGE_NUMBER_ROLEOVER.equalsIgnoreCase (faultSubcodeValue) ||
			Sandesha2Constants.SOAPFaults.Subcodes.SEQUENCE_CLOSED.equalsIgnoreCase (faultSubcodeValue) ||
			Sandesha2Constants.SOAPFaults.Subcodes.SEQUENCE_TERMINATED.equalsIgnoreCase (faultSubcodeValue) ||
			Sandesha2Constants.SOAPFaults.Subcodes.UNKNOWN_SEQUENCE.equalsIgnoreCase (faultSubcodeValue) ) {
		
			return true;
		}
		
		return false;
		
	}
	
	private static void manageIncomingFault (AxisFault fault, MessageContext msgContext) {
	
		if (log.isErrorEnabled())
			log.error(fault);
		
		SandeshaListener listner = (SandeshaListener) msgContext.getProperty(SandeshaClientConstants.SANDESHA_LISTENER);
		if (listner!=null)
			listner.onError(fault);
		
	}
	
	public static void processMessagesForFaults (MessageContext msgContext) {
		
		SOAPEnvelope envelope = msgContext.getEnvelope();
		if (envelope==null) 
			return;
		
		SOAPFault faultPart = envelope.getBody().getFault();

		if (faultPart != null) {

			// constructing the fault
			AxisFault axisFault = getAxisFaultFromFromSOAPFault(faultPart);
			manageIncomingFault (axisFault, msgContext);
		}

	}

	
	private static AxisFault getAxisFaultFromFromSOAPFault(SOAPFault faultPart) {
		AxisFault axisFault = new AxisFault(faultPart.getCode(), faultPart.getReason(), faultPart.getNode(), faultPart
				.getRole(), faultPart.getDetail());

		return axisFault;
	}

}
