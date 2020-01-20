/*
 * This file is part of StarMines: The Next Generation - Copyright 2018-2020 Jari Karjala - www.jpkware.com
 * SPDX-License-Identifier: GPLv3-only
 */
package com.jpkware.smtng

import scala.scalajs.js
import js.Dynamic.{ global => g }

object Logger {
  var messages: Messages = _
  var showInfo: Boolean = false
  def info(s: String): Unit = {
    if (!showInfo) return
    if (messages!=null) messages.show(s)
    g.console.info(s)
  }
  def warn(s: String): Unit = {
    if (messages!=null) messages.show(s)
    g.console.warn(s)
  }
}