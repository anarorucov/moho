package com.voxeo.moho.remote;

/**
 * This exception indicate communication exception with rayo server.
 */
public class MohoRemoteException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MohoRemoteException(String message) {
    super(message);
  }

  public MohoRemoteException(Exception cause) {
    super(cause.getMessage(), cause);
  }

  public MohoRemoteException(String message, Exception cause) {
    super(message, cause);
  }
}
