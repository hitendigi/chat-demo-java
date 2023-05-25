import React, { Component } from "react";
import { Redirect } from 'react-router-dom';
import { connect } from "react-redux";
import WebSocketService from '../services/WebSocketService';

class Profile extends Component {


  render() {

    const { user: currentUser } = this.props;

    if (!currentUser) {
      return <Redirect to="/login" />;
    }

    return (
      <div className="card bg-light text-dark">
        <h1>{currentUser.username}</h1>
        <p>
          <strong>Id:</strong> {currentUser.id}
        </p>
        <p>
          <strong>Email:</strong> {currentUser.email}
        </p>
        <a href="#"><i className="fa fa-dribbble"></i></a>
        <a href="#"><i className="fa fa-twitter"></i></a>
        <a href="#"><i className="fa fa-linkedin"></i></a>
        <a href="#"><i className="fa fa-facebook"></i></a>

        <p>
        Message : <input type="text"></input> <input type="button" value="Send"></input>
        </p>
          </div>
    );

  }
}

function renderUser(){
    const user = JSON.parse(localStorage.getItem("user"));
    if(user){
        console.log('user: ' + user.accessToken)
        const token = 'your-jwt-token';
        const webSocketService = new WebSocketService(user.accessToken);
            webSocketService.connect();
    }
}

function mapStateToProps(state) {
  const { user } = state.auth;
  return {
    user,
  };
}

renderUser();

export default connect(mapStateToProps)(Profile);