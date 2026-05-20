package app.giftify.image.adapter.inbound.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.image.application.port.in.IssuePresignedDownloadUseCase;
import app.giftify.image.application.port.in.IssuePresignedUploadUseCase;
import app.giftify.security.common.CurrentMemberId;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

	private static final Long MEMBER_ID = 42L;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	@MockitoBean
	private IssuePresignedUploadUseCase uploadUseCase;
	@MockitoBean
	private IssuePresignedDownloadUseCase downloadUseCase;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(new ImageController(uploadUseCase, downloadUseCase))
			.setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
				@Override
				public boolean supportsParameter(MethodParameter parameter) {
					return parameter.hasParameterAnnotation(CurrentMemberId.class);
				}

				@Override
				public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
					NativeWebRequest req, WebDataBinderFactory binder) {
					return MEMBER_ID;
				}
			})
			.build();
	}

	@Test
	@DisplayName("POST /presigned-upload 는 PUT URL 을 RsData 로 반환한다")
	void issue_upload_returns_put_url() throws Exception {
		var issued = new IssuePresignedUploadUseCase.PresignedUpload(
			"products/42/abc.jpg", "https://minio:9000/upload?sig=xyz", "PUT", Duration.ofMinutes(5));
		given(uploadUseCase.issue(any())).willReturn(issued);

		PresignedUploadRequestDto body = new PresignedUploadRequestDto("products", "image/jpeg");

		mockMvc.perform(post("/api/v2/images/presigned-upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.key").value("products/42/abc.jpg"))
			.andExpect(jsonPath("$.data.url").value("https://minio:9000/upload?sig=xyz"))
			.andExpect(jsonPath("$.data.httpMethod").value("PUT"))
			.andExpect(jsonPath("$.data.expiresInSeconds").value(300));
	}

	@Test
	@DisplayName("upload 에 인증된 memberId 가 use-case 에 전달된다")
	void upload_passes_authenticated_member_id() throws Exception {
		var issued = new IssuePresignedUploadUseCase.PresignedUpload(
			"products/42/k.jpg", "https://x", "PUT", Duration.ofMinutes(5));
		given(uploadUseCase.issue(any())).willReturn(issued);

		PresignedUploadRequestDto body = new PresignedUploadRequestDto("products", "image/jpeg");

		mockMvc.perform(post("/api/v2/images/presigned-upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk());

		org.mockito.ArgumentCaptor<IssuePresignedUploadUseCase.Command> captor =
			org.mockito.ArgumentCaptor.forClass(IssuePresignedUploadUseCase.Command.class);
		org.mockito.Mockito.verify(uploadUseCase).issue(captor.capture());
		org.junit.jupiter.api.Assertions.assertEquals(MEMBER_ID, captor.getValue().ownerId());
	}

	@Test
	@DisplayName("GET /presigned-download 는 key 쿼리 파라미터로 GET URL 을 반환한다")
	void issue_download_returns_get_url() throws Exception {
		var issued = new IssuePresignedDownloadUseCase.PresignedDownload(
			"products/42/abc.jpg", "https://minio:9000/get?sig=zzz", "GET", Duration.ofMinutes(10));
		given(downloadUseCase.issue(any())).willReturn(issued);

		mockMvc.perform(get("/api/v2/images/presigned-download")
				.param("key", "products/42/abc.jpg"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.key").value("products/42/abc.jpg"))
			.andExpect(jsonPath("$.data.httpMethod").value("GET"))
			.andExpect(jsonPath("$.data.expiresInSeconds").value(600));
	}
}
