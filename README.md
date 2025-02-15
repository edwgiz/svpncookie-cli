[![We recommend IntelliJ IDEA](http://amihaiemil.github.io/images/intellij-idea-recommend.svg)](https://www.jetbrains.com/idea/)

# SVPNCOOKIE CLI
`svpn-cookie-cli` is a command-line utility designed to automate the retrieval of cookie for `openfortivpn`. 
It is an analog to openfortivpn-webview but operates entirely from the command line,
making it ideal for scripting and headless environments.

# Features
- Command-Line Interface: No GUI required; works seamlessly in terminal environments.
- Automated Authentication: Takes login credentials and PIN as arguments to automate the VPN cookie retrieval process.
- Lightweight: Minimal dependencies and easy to integrate into existing workflows.

# Installation
To use svpn-cookie-cli, clone the repository and install the required dependency which is GraalVM.

```shell
git clone https://github.com/edwgiz/svpncookie-cli.git
cd svpncookie-cli
sdk install java 25.ea.8-graal
JAVA_HOME=$( sdk home java 25.ea.8-graal )
./compile.sh
```
Since you build the binary the dependencies can be removed/uninstalled.

# Usage
The tool requires four arguments to function:
- URL of the Fortinet SSL-VPN Portal login page
- username
- password
- authentication code

## Example Command

```shell
./svpn-cookie-cli https://forninet.example.com/remote/login john.smith@example.com my_password 064023 
```
The tool will output the SVPNCOOKIE, which can be used for authentication with `openfortivpn` as follows
```shell
sudo openfortivpn forninet.example.com:443 --cookie=RT/IvJ/p1hAPd...TPba/TQl --trusted-cert f620...1a0e 
```


# License
This project is licensed under the MIT License.

# Support
If you encounter any issues or have questions, please open an issue on the GitHub repository.
