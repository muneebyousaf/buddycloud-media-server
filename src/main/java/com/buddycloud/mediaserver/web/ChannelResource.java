/*
 * Copyright 2012 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.mediaserver.web;

import com.buddycloud.mediaserver.business.dao.DAOFactory;
import com.buddycloud.mediaserver.business.dao.MediaDAO;
import com.buddycloud.mediaserver.commons.Constants;
import com.buddycloud.mediaserver.commons.exception.MetadataSourceException;
import com.buddycloud.mediaserver.commons.exception.MissingAuthenticationException;
import com.buddycloud.mediaserver.commons.exception.UserNotAllowedException;
import com.buddycloud.mediaserver.commons.exception.XMPPException;
import com.buddycloud.mediaserver.xmpp.XMPPToolBox;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource that represents /<channel> endpoint.
 *
 * @author Rodrigo Duarte Sousa - rodrigodsousa@gmail.com
 */
public class ChannelResource extends MediaServerResource {

	@Post("application/x-www-form-urlencoded|multipart/form-data")
	public Representation postMedia(Representation entity) {
		setServerHeader();
		Request request = getRequest();

        try {
            String userJID = getUsedJID(request, true);
            MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();
            String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);

            String result;
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
				result = mediaDAO.insertFormDataMedia(userJID, entityId, getRequest(), false);
			} else {
				result = mediaDAO.insertWebFormMedia(userJID, entityId, new Form(entity), false);
			}
			setStatus(Status.SUCCESS_CREATED);
            return new StringRepresentation(result, MediaType.APPLICATION_JSON);
		} catch (FileUploadException e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (UserNotAllowedException e) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		} catch (MissingAuthenticationException e) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return authenticationResponse();
        } catch (XMPPException e) {
            setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
        } catch (Throwable t) {
            return unexpectedError(t);
        }

        return new EmptyRepresentation();
    }

	/**
	 * Gets media's information list (GET /<channel>) 
	 */
	@Get
	public Representation getMediasInfo() {
		setServerHeader();
		Request request = getRequest();

        try {
            String entityId = (String) request.getAttributes().get(Constants.ENTITY_ARG);

            String userJID = null;
            boolean isChannelPublic = XMPPToolBox.getInstance().getPubSubClient().isChannelPublic(entityId);
            if (!isChannelPublic) {
                userJID = getUsedJID(request, true);
            }

            // Queries
            Integer max = getIntegerQueryValue(Constants.MAX_QUERY);
            String after = getQueryValue(Constants.AFTER_QUERY);

            MediaDAO mediaDAO = DAOFactory.getInstance().getDAO();
            return new StringRepresentation(mediaDAO.getMediasInfo(userJID,
					entityId, max, after), MediaType.APPLICATION_JSON);
		} catch (MetadataSourceException e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
		} catch (UserNotAllowedException e) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		} catch (MissingAuthenticationException e) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return authenticationResponse();
        } catch (XMPPException e) {
            setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
        } catch (Throwable t) {
            return unexpectedError(t);
        }

        return new EmptyRepresentation();
	}
}
