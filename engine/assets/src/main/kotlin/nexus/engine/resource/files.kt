package nexus.engine.resource

import com.google.common.base.Charsets
import nexus.engine.assets.AssetData
import nexus.engine.assets.format.FileFormat
import nexus.engine.assets.format.FileReference
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.*
import javax.annotation.concurrent.Immutable

