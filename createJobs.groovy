pipelineJob('cv-service-job') {
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url 'https://github.com/yamiko/cv_service.git'
                    }
                    branch 'master'
                }
            }
        }
    }
}
