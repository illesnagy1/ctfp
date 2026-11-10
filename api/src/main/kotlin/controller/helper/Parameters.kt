package com.ctfp.controller.helper

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException

fun ApplicationCall.getId(): Int = parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")
