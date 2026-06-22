package com.tubitech.copilot.api;

import com.tubitech.copilot.api.model.SupportedModel;
import java.io.IOException;
import java.util.List;

public interface ModelApiClient {
   List<SupportedModel> fetchSupportedModels() throws IOException;
}
