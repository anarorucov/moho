package com.voxeo.moho.sip;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import com.voxeo.moho.SignalException;
import com.voxeo.moho.TextableEndpoint;
import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.event.MohoTextEvent;
import com.voxeo.moho.sip.SIPTextEvent;
import com.voxeo.moho.spi.ExecutionContext;

public class SIPTextEventImpl<T extends EventSource> extends MohoTextEvent<T> implements SIPTextEvent<T> {

  protected SipServletRequest _req;

  protected ExecutionContext _ctx;

  protected SIPTextEventImpl(final T source, final SipServletRequest req) {
    super(source);
    _req = req;
    _ctx = (ExecutionContext) source.getApplicationContext();
  }

  @Override
  public SipServletRequest getSipRequest() {
    return _req;
  }
  
  @Override
  public String getText() {
    Object content = null;
    try {
      content = _req.getContent();
    }
    catch (final IOException e) {
      throw new SignalException(e);
    }
    return content == null ? null : String.valueOf(content);
  }

  @Override
  public String getTextType() {
    return _req.getContentType();
  }

  @Override
  public TextableEndpoint getFrom() {
    return new SIPEndpointImpl(_ctx, _req.getFrom());
  }

  @Override
  public TextableEndpoint getTo() {
    return new SIPEndpointImpl(_ctx, _req.getTo());
  }

}