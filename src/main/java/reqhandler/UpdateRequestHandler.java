package reqhandler;

import constant.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("UpdateRequestHandler")
public class UpdateRequestHandler extends RequestHandler {

    @Value("${core.android.current_version}")
    private String cuAndroidVersion;

    @Value("${core.ios.current_version}")
    private String cuIOSVersion;

    @Value("${core.android.download_url}")
    private String androidDownloadUrl;

    @Value("${core.ios.download_url}")
    private String iosDownloadUrl;

    public void check() {
        String clientOS = this.request.getHeader("User-Agent");
        String clientVersion = this.request.getHeader("App-Version");
        if (clientOS == null) {
            this.responseData.put("status", Status.INVALID_USER_AGENT);
            this.responseData.put("message", "Invalid user agent");
            return;
        }
        if (clientVersion == null) {
            this.responseData.put("status", Status.INVALID_APP_VERSION);
            this.responseData.put("message", "Invalid app version");
            return;
        }
        this.responseData.put("status", Status.UN_NEED_UPDATE);
        this.responseData.put("msg", "Current version is latest");
        if (clientOS.toLowerCase().contains("android") && !clientVersion.equalsIgnoreCase(cuAndroidVersion)) {
            this.responseData.put("status", Status.NEED_UPDATE);
            this.responseData.put("msg", "Need update");
            this.responseData.put("url", androidDownloadUrl);
        } else if (clientOS.toLowerCase().contains("ios") && !clientVersion.equalsIgnoreCase(cuIOSVersion)) {
            this.responseData.put("status", Status.NEED_UPDATE);
            this.responseData.put("msg", "Need update");
            this.responseData.put("url", iosDownloadUrl);
        }
    }

}
