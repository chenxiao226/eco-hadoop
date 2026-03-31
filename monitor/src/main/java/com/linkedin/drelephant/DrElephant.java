/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.linkedin.drelephant;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The main class which starts Dr. Elephant
 */
@Component
public class DrElephant extends Thread {

	@Autowired
  private ElephantRunner _elephantRunner;

  public DrElephant() throws IOException {
	  //_elephantRunner = new ElephantRunner(); //通过ElephantRunner启动线程
  }

  @Override
  public void run() {
	  System.out.println("任务分析启动.......");
	  _elephantRunner.run();
  }

  public void kill() {
    if (_elephantRunner != null) {
    	_elephantRunner.kill();
    }
  }
}
