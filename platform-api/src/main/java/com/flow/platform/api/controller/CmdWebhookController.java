/*
 * Copyright 2017 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flow.platform.api.controller;

import com.flow.platform.api.config.QueueConfig;
import com.flow.platform.api.domain.CmdCallbackQueueItem;
import com.flow.platform.api.service.job.JobService;
import com.flow.platform.core.exception.IllegalParameterException;
import com.flow.platform.domain.Cmd;
import com.flow.platform.util.http.HttpURL;
import com.google.common.base.Strings;
import java.math.BigInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to receive cmd webhook from flow control center
 *
 * @author yh@firim
 */
@Log4j2
@RestController
@RequestMapping("/hooks/cmd")
public class CmdWebhookController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public void execute(@RequestBody Cmd cmd, @RequestParam String identifier) {
        String jobIdStr = HttpURL.encode(identifier);

        if (Strings.isNullOrEmpty(jobIdStr)) {
            throw new IllegalParameterException("Invalid 'identifier' parameter");
        }

        if (cmd.getType() == null) {
            throw new IllegalParameterException("Invalid cmd request data");
        }

        try {
            BigInteger jobId = new BigInteger(jobIdStr);
            log.trace("Cmd Webhook received: Cmd [{} : {} : {} : {}]",
                cmd.getType(),
                cmd.getStatus(),
                cmd.getId(),
                cmd.getCmdResult());
            jobService.enqueue(new CmdCallbackQueueItem(jobId, cmd), QueueConfig.DEFAULT_CMD_CALLBACK_QUEUE_PRIORITY);
        } catch (NumberFormatException warn) {
            log.warn("Invalid job id format");
        }
    }
}