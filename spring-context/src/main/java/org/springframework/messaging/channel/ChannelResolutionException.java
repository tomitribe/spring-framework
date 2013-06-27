/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.messaging.channel;

import org.springframework.messaging.MessagingException;

/**
 * Thrown by a ChannelResolver when it cannot resolve a channel name.
 *
 * @author Mark Fisher
 * @since 4.0
 */
@SuppressWarnings("serial")
public class ChannelResolutionException extends MessagingException {

	/**
	 * Create a new ChannelResolutionException.
	 * @param description the description
	 */
	public ChannelResolutionException(String description) {
		super(description);
	}

	/**
	 * Create a new ChannelResolutionException.
	 * @param description the description
	 * @param cause the root cause (if any)
	 */
	public ChannelResolutionException(String description, Throwable cause) {
		super(description, cause);
	}

}
