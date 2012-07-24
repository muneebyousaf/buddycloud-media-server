package com.buddycloud.mediaserver.web;
import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.buddycloud.mediaserver.business.dao.DAOFactory;
import com.buddycloud.mediaserver.business.dao.MediaDAO;
import com.buddycloud.mediaserver.commons.Constants;
import com.buddycloud.mediaserver.commons.exception.FormFieldException;
import com.buddycloud.mediaserver.commons.exception.InvalidPreviewFormatException;
import com.buddycloud.mediaserver.commons.exception.MediaNotFoundException;
import com.buddycloud.mediaserver.commons.exception.MetadataSourceException;
import com.buddycloud.mediaserver.commons.exception.UserNotAllowedException;
import com.buddycloud.mediaserver.web.representation.DynamicFileRepresentation;


public class MediaResource extends MediaServerResource {
	
	
	@Post
	public Representation postAvatar(Representation entity) {
		String auth = getQueryValue(Constants.AUTH_QUERY);
		
		String userId = null;
		String token = null;

		Request request = getRequest();
		if (auth == null) {
			ChallengeResponse challenge = request.getChallengeResponse();
			if (challenge == null) {
				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				return authenticationResponse();
			} else {
				userId = challenge.getIdentifier();
				token = new String(challenge.getSecret());
			}
		} else {
			

		}
		if (!verifyRequest(userId, token,request.getResourceRef().getIdentifier())) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("User '" + userId + "' not allowed to access resource", MediaType.APPLICATION_JSON);
		}

		if (entity != null) {
			if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
				String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);
				String mediaId = (String) request.getAttributes().get(Constants.MEDIA_ARG);

				if (!mediaId.equals(Constants.AVATAR_ARG)) {
					setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
				}

				MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();
				
				try {
					return new StringRepresentation(mediaDAO.insertMedia(userId, entityId, getRequest(), true), 
							MediaType.APPLICATION_JSON);
				} catch (FileUploadException e) {
					setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (MetadataSourceException e) {
					setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (FormFieldException e) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (UserNotAllowedException e) {
					setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				}
			}
		}

		// POST request with no entity.
		setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return new StringRepresentation("POST request with no entity", MediaType.APPLICATION_JSON);
	}

	@Delete
	public Representation deleteMedia() {
		ChallengeResponse challenge = getChallengeResponse();
		
		if (challenge == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			
			return authenticationResponse();
		}
		
		String userId = challenge.getIdentifier();
		Request request = getRequest();
		
		if (!verifyRequest(userId, new String(challenge.getSecret()), request.getResourceRef().getIdentifier())) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("User '" + userId + "' not allowed to access resource", MediaType.APPLICATION_JSON);
		}
		
		String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);
		String mediaId = (String) request.getAttributes().get(Constants.MEDIA_ARG);

		MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();
		
		try {
			mediaDAO.deleteMedia(userId, entityId, mediaId);
			return new StringRepresentation("Media deleted", MediaType.APPLICATION_JSON);
		} catch (MetadataSourceException e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		} catch (MediaNotFoundException e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		} catch (UserNotAllowedException e) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		}
	}

	@Put
	public Representation putMedia(Representation entity) {
		ChallengeResponse challenge = getChallengeResponse();
		
		if (challenge == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			
			return authenticationResponse();
		}
		
		String userId = challenge.getIdentifier();
		Request request = getRequest();
		
		if (!verifyRequest(userId, new String(challenge.getSecret()), request.getResourceRef().getIdentifier())) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("User '" + userId + "' not allowed to access resource", MediaType.APPLICATION_JSON);
		}
		
		String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);
		String mediaId = (String) request.getAttributes().get(Constants.MEDIA_ARG);

		if (entity != null) {
			if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {

				MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();
				
				try {
					return new StringRepresentation(mediaDAO.updateMedia(userId, entityId, mediaId, getRequest()), 
							MediaType.APPLICATION_JSON);
				} catch (FileUploadException e) {
					setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (MetadataSourceException e) {
					setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (FormFieldException e) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (MediaNotFoundException e) {
					setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				} catch (UserNotAllowedException e) {
					setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
				}
			}
		}

		// POST request with no entity.
		setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return new StringRepresentation("POST request with no entity", MediaType.APPLICATION_JSON);
	}

	@Get
	public Representation getMedia() {
		ChallengeResponse challenge = getChallengeResponse();
		
		if (challenge == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			
			return authenticationResponse();
		}
		
		String userId = challenge.getIdentifier();
		Request request = getRequest();
		
		if (!verifyRequest(userId, new String(challenge.getSecret()), request.getResourceRef().getIdentifier())) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("User '" + userId + "' not allowed to access resource", MediaType.APPLICATION_JSON);
		}
		
		String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);
		String mediaId = (String) request.getAttributes().get(Constants.MEDIA_ARG);

		String maxHeight = getQueryValue(Constants.MAX_HEIGHT_QUERY);
		String maxWidth = getQueryValue(Constants.MAX_WIDTH_QUERY);

		MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();

		try {
			MediaType mediaType = new MediaType(mediaDAO.getMediaType(entityId, mediaId));

			if (maxHeight == null && maxWidth == null) {
				File media = mediaDAO.getMedia(userId, entityId, mediaId);
				return new FileRepresentation(media, mediaType);
			}
			
			byte[] preview = null;

			if (maxHeight != null && maxWidth == null) {
				preview = mediaDAO.getMediaPreview(userId, entityId, mediaId, Integer.valueOf(maxHeight));
			} else if (maxHeight == null && maxWidth != null) {
				preview = mediaDAO.getMediaPreview(userId, entityId, mediaId, Integer.valueOf(maxWidth));
			} else {
				preview = mediaDAO.getMediaPreview(userId, entityId, mediaId, Integer.valueOf(maxHeight), Integer.valueOf(maxWidth));
			}

			return new DynamicFileRepresentation(mediaType, preview);
		} catch (MetadataSourceException e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		} catch (IOException e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);	
		} catch (MediaNotFoundException e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		} catch (InvalidPreviewFormatException e) {
			setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		} catch (UserNotAllowedException e) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation(e.getMessage(), MediaType.APPLICATION_JSON);
		}
	}
}
