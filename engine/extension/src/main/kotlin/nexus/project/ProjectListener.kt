package nexus.project

interface ProjectListener {
    fun onProjectStart(application: Project) {}
    fun onProjectStop(application: Project) {}
}