package com.voxeo.moho.sip;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

import com.voxeo.moho.Participant.JoinType;
import com.voxeo.moho.event.JoinCompleteEvent;
import com.voxeo.moho.sip.SIPCall.State;

public class DirectNO2MultipleNOJoinDelegate extends JoinDelegate {
  private static final Logger LOG = Logger.getLogger(DirectNO2MultipleNOJoinDelegate.class);

  protected List<SIPCallImpl> candidateCalls = new LinkedList<SIPCallImpl>();

  protected boolean _suppressEarlyMedia;

  protected SipServletResponse _responseCall1;

  protected SipServletResponse _responseCall2;

  protected Object syncLock = new Object();

  protected DirectNO2MultipleNOJoinDelegate(JoinType type, Direction direction, SIPCallImpl call1,
      boolean suppressEarlyMedia, List<SIPCallImpl> others) {
    _call1 = call1;
    _suppressEarlyMedia = suppressEarlyMedia;
    _direction = direction;
    _joinType = type;
    candidateCalls.addAll(others);
  }

  @Override
  public void doJoin() throws Exception {
    super.doJoin();
    // TODO should we use mock SDP (SDP connection address 0.0.0.0 or with
    // sendonly
    // atrribute) to disable 183 here?
    SIPHelper.remove100relSupport(_call1.getSipInitnalRequest());
    ((SIPOutgoingCall) _call1).call(null);
  }

  @Override
  protected void doInviteResponse(SipServletResponse res, SIPCallImpl call, Map<String, String> headers)
      throws Exception {
    try {
      synchronized (syncLock) {
        if (SIPHelper.isProvisionalResponse(res)) {
          SIPHelper.trySendPrack(res);
          return;
        }
        else if (SIPHelper.isSuccessResponse(res)) {
          if (_call1.equals(call)) {
            _responseCall1 = res;
            _call1.setSIPCallState(State.ANSWERED);

            Exception exception = null;
            int exceptionCount = 0;

            for (SIPCallImpl candidate : candidateCalls) {
              try {
                SIPHelper.remove100relSupport(candidate.getSipInitnalRequest());
                ((SIPOutgoingCall) call).call(_call1.getRemoteSdp(), _call1.getSipSession().getApplicationSession());
              }
              catch (Exception ex) {
                exceptionCount++;
                exception = ex;
                LOG.error("Exception when trying call " + call, ex);
              }

              if (exceptionCount == candidateCalls.size()) {
                LOG.error("Exception when joining using delegate " + this, exception);
                done(JoinCompleteEvent.Cause.ERROR, exception);
                failCall(_call1, exception);
                throw exception;
              }
            }
          }
          else {
            res.createAck().send();

            if (_call2 != null) {
              // receive second success response, ignore it.
              return;
            }

            _responseCall2 = res;
            _call2 = call;
            candidateCalls.remove(call);
            disconnectCalls(candidateCalls);
            _call2.setSIPCallState(State.ANSWERED);

            SipServletRequest ack1 = _responseCall1.createAck();
            SIPHelper.copyContent(_responseCall2, ack1);
            ack1.send();

            successJoin();
          }
        }
        else if (SIPHelper.isErrorResponse(res)) {
          if (_call1.equals(call)) {
            LOG.warn("INVITE call1 got error response, failed join on delegate " + this);
            done(this.getJoinCompleteCauseByResponse(res), this.getExceptionByResponse(res));
            this.disconnectCall(_call1, true, getCallCompleteCauseByResponse(res), getExceptionByResponse(res));
          }
          else {
            candidateCalls.remove(call);
            if (candidateCalls.isEmpty() && _call2 == null) {
              _call2 = call;
              done(this.getJoinCompleteCauseByResponse(res), this.getExceptionByResponse(res));
            }

            disconnectCall(call, true, getCallCompleteCauseByResponse(res), getExceptionByResponse(res));

            try {
              if (_responseCall1 != null) {
                _responseCall1.createAck().send();
              }
            }
            catch (Exception ex) {
              LOG.debug("Exception when sending back ACK", ex);
            }
            disconnectCall(_call1, true, getCallCompleteCauseByResponse(res), getExceptionByResponse(res));
          }
        }
      }
    }
    catch (Exception ex) {
      LOG.error("Exception when joining using delegate " + this, ex);
      done(JoinCompleteEvent.Cause.ERROR, ex);
      failCall(_call1, ex);
      if (_call2 != null) {
        failCall(_call2, ex);
      }
      if (!candidateCalls.isEmpty()) {
        disconnectCalls(candidateCalls);
      }
      throw ex;
    }
  }

  private void successJoin() throws MsControlException {
    _peer = _call2;
    doDisengage(_call1, JoinType.DIRECT);
    _call1.linkCall(_call2, JoinType.DIRECT, _direction);
    done(JoinCompleteEvent.Cause.JOINED, null);
  }
}
