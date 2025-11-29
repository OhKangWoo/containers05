pipeline {
    agent {
        label 'ansible-agent'
    }
    
    environment {
        ANSIBLE_REPO = 'https://github.com/OhKangWoo/containers05.git'
        ANSIBLE_BRANCH = 'main'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository with Ansible playbook...'
                git branch: "${ANSIBLE_BRANCH}", 
                    url: "${ANSIBLE_REPO}"
            }
        }
        
        stage('Verify Ansible Installation') {
            steps {
                echo 'Checking Ansible version...'
                sh 'ansible --version'
            }
        }
        
        stage('Test Connection to Test Server') {
            steps {
                echo 'Testing connection to test server...'
                sh '''
                    cd ansible
                    ansible test_servers -i hosts.ini -m ping
                '''
            }
        }
        
        stage('Run Ansible Playbook') {
            steps {
                echo 'Executing Ansible playbook for test server configuration...'
                sh '''
                    cd ansible
                    ansible-playbook -i hosts.ini setup_test_server.yml -v
                '''
            }
        }
        
        stage('Verify Apache Installation') {
            steps {
                echo 'Verifying Apache is running on test server...'
                sh '''
                    cd ansible
                    ansible test_servers -i hosts.ini -m shell -a "systemctl status apache2"
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Ansible pipeline completed.'
        }
        success {
            echo 'Test server configured successfully!'
        }
        failure {
            echo 'Configuration failed. Check the logs for details.'
        }
    }
}