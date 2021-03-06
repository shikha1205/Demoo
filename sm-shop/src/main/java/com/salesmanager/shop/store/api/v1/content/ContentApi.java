package com.salesmanager.shop.store.api.v1.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.content.ContentFile;
import com.salesmanager.shop.model.content.ContentFolder;
import com.salesmanager.shop.store.controller.content.facade.ContentFacade;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.utils.LanguageUtils;

@Controller
@RequestMapping("/api/v1")
public class ContentApi {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentApi.class);
	
	@Inject
	private ContentFacade contentFacade;
	
	@Inject
	private StoreFacade storeFacade;
	
	@Inject
	private LanguageUtils languageUtils;
	
	@Inject
	private ContentService contentService;
	
/*	@RequestMapping( value={"/private/content/settings"}, method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ContentSettings settings(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			
			MerchantStore merchantStore = storeFacade.getByCode(request);
			Language language = languageUtils.getRESTLanguage(request, merchantStore);
			

			//scheme [SHOP_SCHEME]
			//String scheme = request.getScheme();
			
			//host [determin host]
			//String hostname = InetAddress.getLocalHost().getHostName();
			
			//port [determine port]
			//int port = request.getServerPort();
			
			
			int port = request.getServerPort();
			StringBuilder result = new StringBuilder();
			result.append(request.getScheme())
			        .append("://")
			        .append(request.getServerName());

			if ( (request.getScheme().equals("http") && port != 80) || (request.getScheme().equals("https") && port != 443) ) {
				result.append(':')
					.append(port);
			}

			result.append(request.getContextPath());
			
			ContentSettings settings = new ContentSettings();
			settings.setHttpBasePath(result.toString());
			
			return settings;

			
			
		} catch (Exception e) {
			LOGGER.error("Error while building content settings",e);
			response.sendError(503, "Error while building content settings " + e.getMessage());
		}
		
		return null;
	}*/
	
	
	@RequestMapping( value={"/content/folder"}, method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ContentFolder folder(@RequestParam(value = "path", required=false) String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			
			MerchantStore merchantStore = storeFacade.getByCode(request);
			Language language = languageUtils.getRESTLanguage(request, merchantStore);
			
			if(!StringUtils.isBlank(path)) {
				path = URLDecoder.decode(path,"UTF-8");
			}
			
			ContentFolder folder = contentFacade.getContentFolder(path, merchantStore);

			
			if(folder == null){
				response.sendError(404, "No Folder found for path : " + path);
			}
			
			return folder;
			
			
		} catch (Exception e) {
			LOGGER.error("Error while getting folder",e);
			try {
				response.sendError(503, "Error while getting folder " + e.getMessage());
			} catch (Exception ignore) {
			}
		}
		
		return null;
	}
	
	/**
	 * Need type, name and entity
	 * @param requestEntity
	 * @param fileName
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping( value={"/private/content"}, method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public HttpEntity<String> upload(@Valid @RequestBody ContentFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			
			MerchantStore merchantStore = storeFacade.getByCode(request);
			Language language = languageUtils.getRESTLanguage(request, merchantStore);
			

			byte[] payload = file.getFile();
			InputStream targetStream = new ByteArrayInputStream(payload);
		    
			String fileName = file.getName();
			
            String type = file.getContentType() .split("/")[0];
            FileContentType fileType = FileContentType.STATIC_FILE;
            if(type.equals("image")){
            	fileType = FileContentType.IMAGE;
            } 

            InputContentFile cmsContent = new InputContentFile();
            cmsContent.setFileName(fileName);
            cmsContent.setMimeType( file.getContentType() );
            cmsContent.setFile( targetStream );
            cmsContent.setFileContentType(fileType);

            contentService.addContentFile( merchantStore.getCode(), cmsContent );
            
            String fileUrl = contentFacade.absolutePath(merchantStore, fileName);
            HttpEntity<String> responseEntity = new HttpEntity<>(fileUrl);
            return responseEntity;
			
	
		} catch (Exception e) {
			LOGGER.error("Error while adding file folder",e);
			try {
				response.sendError(503, "Error while adding file " + e.getMessage());
			} catch (Exception ignore) {
			}
			
		}
		
		return null;

	}
	


}
