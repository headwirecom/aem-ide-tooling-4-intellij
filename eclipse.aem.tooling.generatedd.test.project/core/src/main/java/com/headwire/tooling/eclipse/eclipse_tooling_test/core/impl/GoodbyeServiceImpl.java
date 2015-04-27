/*
 *  Copyright 2014 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.headwire.tooling.eclipse.eclipse_tooling_test.core.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(GoodbyeServiceImpl.class)
public class GoodbyeServiceImpl
//    implements HelloService
{

//	@Reference
//	private SlingSettingsService settings;
	
//	@Override
	public String getMessage() {
//		return "Hello World, this is instance for AEM Tooling Test 4 IntelliJ 3" + settings.getSlingId();
		return "Goodbye World, this is instance for AEM Tooling Test 4 IntelliJ";
	}

}
