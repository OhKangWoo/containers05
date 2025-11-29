pipeline {
    agent {
        label 'ansible-agent'
    }
    
    environment {
        PHP_PROJECT_REPO = 'https://github.com/OhKangWoo/containers05.git'
        PHP_PROJECT_BRANCH = 'main'
        DEPLOY_PATH = '/var/www/html/php-app'
    }
    
    stages {
        stage('Checkout PHP Project') {
            steps {
                echo 'Cloning PHP project repository...'
                git branch: "${PHP_PROJECT_BRANCH}", 
                    url: "${PHP_PROJECT_REPO}"
            }
        }
        
        stage('Create Deployment Playbook') {
            steps {
                echo 'Creating deployment playbook...'
                sh '''
                    cat > deploy.yml << 'EOF'
---
- name: Deploy PHP Application to Test Server
  hosts: test_servers
  become: yes
  vars:
    app_directory: "/var/www/html/php-app"
    source_directory: "{{ lookup('env', 'WORKSPACE') }}"

  tasks:
    - name: Ensure application directory exists
      file:
        path: "{{ app_directory }}"
        state: directory
        owner: www-data
        group: www-data
        mode: '0755'

    - name: Synchronize application files
      synchronize:
        src: "{{ source_directory }}/"
        dest: "{{ app_directory }}/"
        delete: yes
        recursive: yes
      delegate_to: localhost

    - name: Set proper ownership
      file:
        path: "{{ app_directory }}"
        owner: www-data
        group: www-data
        recurse: yes

    - name: Set proper permissions for directories
      shell: find {{ app_directory }} -type d -exec chmod 755 {} \\;

    - name: Set proper permissions for files
      shell: find {{ app_directory }} -type f -exec chmod 644 {} \\;

    - name: Restart Apache to apply changes
      systemd:
        name: apache2
        state: restarted
EOF
                '''
            }
        }
        
        stage('Deploy Application') {
            steps {
                echo 'Deploying PHP application to test server...'
                sh '''
                    cd ${WORKSPACE}
                    ansible-playbook -i ansible/hosts.ini deploy.yml -v
                '''
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment...'
                sh '''
                    ansible test_servers -i ansible/hosts.ini -m shell \
                        -a "ls -la /var/www/html/php-app"
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Deployment pipeline completed.'
            cleanWs()
        }
        success {
            echo 'PHP application deployed successfully!'
        }
        failure {
            echo 'Deployment failed. Check the logs for details.'
        }
    }
}