/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edorasware.one.initializr.web.mapper;

import com.edorasware.one.initializr.metadata.DependencyMetadata;

/**
 * Generate a JSON representation of a set of dependencies.
 *
 * @author Stephane Nicoll
 */
interface DependencyMetadataJsonMapper {

	/**
	 * Write a json representation of the specified metadata.
	 */
	String write(DependencyMetadata metadata);

}
