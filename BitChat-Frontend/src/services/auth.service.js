import axios from "axios";

const API_URL = "https://localhost:8888/api/auth/";

class AuthService {
  login(username, name) {
    return axios
      .post(API_URL + "signin", { username, name })
      .then((response) => {
        if (response.data.accessToken) {
          //console.log('jwt token : ' + response.data.accessToken);
          localStorage.setItem("user", JSON.stringify(response.data));
        }

        return response.data;
      });
  }

  logout() {
    localStorage.removeItem("user");
  }

  register(name, username, email, password) {
    return axios.post(API_URL + "signup", {
      name,
      username,
      email,
      password,
    });
  }
}

export default new AuthService();
