/*
 * Copyright (C) 2020 Dominik Schadow, dominikschadow@gmail.com
 *
 * This file is part of the Cloud Security project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dominikschadow.standalone.home;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to return the public home page of the application.
 *
 * @author Dominik Schadow
 */
@Controller
public class HomeController {
    @Value("${client.encrypted-database-password}")
    private String encryptedDatasourcePassword;

    /**
     * Returns a greeting containing the applications name.
     *
     * @return The greeting
     */
    @GetMapping(value = "/")
    public String home(final Model model) {
        model.addAttribute("encryptedDatasourcePassword", encryptedDatasourcePassword);

        return "index";
    }
}