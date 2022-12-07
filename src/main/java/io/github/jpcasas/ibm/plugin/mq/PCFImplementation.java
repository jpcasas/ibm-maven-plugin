package io.github.jpcasas.ibm.plugin.mq;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessageAgent;


public class PCFImplementation extends PCFMessageAgent {

	public PCFImplementation(MQQueueManager mq) throws MQDataException {
		super(mq);
	}

	protected synchronized void open(MQQueueManager qmanagerP, String targetQueueP, String targetQmanager,
			boolean external) throws MQDataException {

		String targetQueue = targetQueueP;
		try {
			disconnect();
		} catch (MQDataException mqe) {
			
		}

		if ((targetQueue == null) || (targetQueue.length() == 0)) {
			try {
				targetQueue = qmanagerP.getCommandInputQueueName();
			} catch (Exception e) {
				MQDataException traceRet1 = MQDataException.getMQDataException(e);
				throw traceRet1;
			}
		}

		String prefix = this.replyQueuePrefix;

		try {
			try {
				this.adminQueue = qmanagerP.accessQueue(targetQueue, 8240);
			} catch (MQException e) {
				if (e.getReason() == 2045) {
					this.adminQueue = qmanagerP.accessQueue(targetQueue, 8208);
				} else {
					throw e;
				}
			}
			int options = /* MQConstants.MQOO_READ_AHEAD |*/ MQConstants.MQOO_OUTPUT; 
			this.replyQueue = qmanagerP.accessQueue(this.modelQueueName, options, "","MQMON.JPC_Puller*", "mqm");
			String queueToOpen = this.replyQueue.getName();
			int openOptions = MQConstants.MQOO_INPUT_EXCLUSIVE | MQConstants.MQOO_FAIL_IF_QUIESCING;
			this.replyQueue = qmanagerP.accessQueue(queueToOpen, openOptions);
			
		} catch (Exception e) {
			MQDataException traceRet2 = MQDataException.getMQDataException(e);

			throw traceRet2;
		}

		this.replyQueueName = this.replyQueue.name;

		this.replyQueue.closeOptions = 2;

	}

}

