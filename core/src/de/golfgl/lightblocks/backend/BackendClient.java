package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Benjamin Schulte on 12.09.2018.
 * <p>
 * Sorry, this file is not open-sourced completely to prevent tampering the backend.
 */

public class BackendClient {

    public static final int SC_NO_CONNECTION = 0;
    private static final String BASE_URL = "https://lightblocks-backend.golfgl.de";
    private static final String LOG_TAG = "BACKEND";
    private String userId;
    private String userPass;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public void createPlayer(String nickName, final IBackendResponse<PlayerCreatedInfo> callback) {
        createPlayer(nickName, null, callback);
    }

    public void createPlayer(String nickName, String gameService, final IBackendResponse<PlayerCreatedInfo> callback) {
        new PlayerCreatedResponseHandler(callback).cancelled();
    }

    public void fetchPlayerDetails(String playerId, final IBackendResponse<PlayerDetails> callback) {
        Map<String, String> params = null;

        final Net.HttpRequest httpRequest = buildRequest("v1/player/" + playerId, params);
        httpRequest.setMethod(Net.HttpMethods.GET);

        Gdx.net.sendHttpRequest(httpRequest, new HttpResponseHandler<PlayerDetails>(callback) {
            @Override
            PlayerDetails parseJsonResponse(JsonValue json) {
                return new PlayerDetails(json);
            }
        });
    }

    public void fetchReplay(String replayUri, final IBackendResponse<String> callback) {
        final Net.HttpRequest httpRequest = buildRequest("v1/replay/" + replayUri, null);
        httpRequest.setMethod(Net.HttpMethods.GET);

        Gdx.net.sendHttpRequest(httpRequest, new HttpResponseHandler<String>(callback) {
            @Override
            String parseJsonResponse(JsonValue json) {
                return json.getString("replay", null);
            }
        });

    }

    public void fetchPlayerByNicknamePrefixList(String nickname, IBackendResponse<List<PlayerDetails>> callback) {
        final Net.HttpRequest httpRequest = buildRequest("v1/nickname/" + nickname, null);
        httpRequest.setMethod(Net.HttpMethods.GET);

        Gdx.net.sendHttpRequest(httpRequest, new HttpResponseHandler<List<PlayerDetails>>(callback) {
            @Override
            List<PlayerDetails> parseJsonResponse(JsonValue json) {
                List<PlayerDetails> playersList = new ArrayList<PlayerDetails>();

                for (JsonValue player = json.child; player != null; player = player.next) {
                    playersList.add(new PlayerDetails(player));
                }

                return playersList;
            }
        });
    }

    public void fetchStrongestMatchPlayersList(IBackendResponse<List<PlayerDetails>> callback) {
        final Net.HttpRequest httpRequest = buildRequest("v1/matches/strongestPlayers", null);
        httpRequest.setMethod(Net.HttpMethods.GET);

        Gdx.net.sendHttpRequest(httpRequest, new HttpResponseHandler<List<PlayerDetails>>(callback) {
            @Override
            List<PlayerDetails> parseJsonResponse(JsonValue json) {
                List<PlayerDetails> playersList = new ArrayList<PlayerDetails>();

                for (JsonValue player = json.child; player != null; player = player.next) {
                    playersList.add(new PlayerDetails(player));
                }

                return playersList;
            }
        });
    }

    public void fetchLatestScores(String gameMode, IBackendResponse<List<ScoreListEntry>> callback) {
        fetchScores(gameMode, true, callback);
    }

    public void fetchBestScores(String gameMode, IBackendResponse<List<ScoreListEntry>> callback) {
        fetchScores(gameMode, false, callback);
    }

    private void fetchScores(final String gameMode, final boolean latest, IBackendResponse<List<ScoreListEntry>>
            callback) {
        final Net.HttpRequest httpRequest = buildRequest("v1/scores/" + gameMode + (latest ? "/latest" : "/best"),
                null);
        httpRequest.setMethod(Net.HttpMethods.GET);

        Gdx.net.sendHttpRequest(httpRequest, new HttpResponseHandler<List<ScoreListEntry>>(callback) {
            @Override
            List<ScoreListEntry> parseJsonResponse(JsonValue json) {
                List<ScoreListEntry> scoreList = new ArrayList<ScoreListEntry>();
                ScoreListEntry.ScoreType scoreType = latest ? ScoreListEntry.ScoreType.latest :
                        ScoreListEntry.ScoreType.best;

                for (JsonValue score = json.child; score != null; score = score.next) {
                    scoreList.add(new ScoreListEntry(score, gameMode, scoreType));
                }

                return scoreList;
            }
        });

    }

    public void fetchWelcomeMessages(int clientVersion, String logicalPlatform, String operatingSystem, long
            drawnBlocks, int donatorState, long lastRequestMs, String pushPlatformId, String pushToken,
                                     IBackendResponse<BackendWelcomeResponse> callback) {

        (new HttpResponseHandler<BackendWelcomeResponse>(callback) {
            @Override
            BackendWelcomeResponse parseJsonResponse(JsonValue json) {
                return new BackendWelcomeResponse(json);
            }
        }).cancelled();

    }

    public void postScore(BackendScore score, IBackendResponse<Void> callback) {
        (new HttpResponseHandler<Void>(callback) {
            @Override
            Void parseJsonResponse(JsonValue json) {
                return null;
            }
        }).cancelled();
    }

    /**
     * Ein Parameter sollte nicht null sein, alle anderen können null sein
     */
    public void changePlayerDetails(String newNickname, String newMailAddress, int donatorState, String newDecoration,
                                    String newPublicContact, IBackendResponse<Void> callback) {

        (new HttpResponseHandler<Void>(callback) {
            @Override
            Void parseJsonResponse(JsonValue json) {
                return null;
            }
        }).cancelled();
    }

    public void deletePlayer(IBackendResponse<Void> callback) {
        (new HttpResponseHandler<Void>(callback) {
            @Override
            Void parseJsonResponse(JsonValue json) {
                return null;
            }
        }).cancelled();
    }

    public void requestActivationCode(String nickname, String mailAddress, IBackendResponse<Void> callback) {
        (new HttpResponseHandler<Void>(callback) {
            @Override
            Void parseJsonResponse(JsonValue json) {
                return null;
            }
        }).cancelled();
    }

    public void linkProfile(String nickname, int activationCode, IBackendResponse<PlayerCreatedInfo> callback) {
        new PlayerCreatedResponseHandler(callback).cancelled();
    }

    public void openNewMatch(String opponentId, int maxLevel, IBackendResponse<MatchEntity> callback) {
        (new HttpResponseHandler<MatchEntity>(callback) {
            @Override
            MatchEntity parseJsonResponse(JsonValue json) {
                return new MatchEntity(json);
            }
        }).cancelled();

    }

    public void listPlayerMatches(long sinceTime, IBackendResponse<Array<MatchEntity>> callback) {
        (new HttpResponseHandler<Array<MatchEntity>>(callback) {
            @Override
            Array<MatchEntity> parseJsonResponse(JsonValue json) {
                Array<MatchEntity> list = new Array<>();

                for (JsonValue match = json.child; match != null; match = match.next) {
                    list.add(new MatchEntity(match));
                }

                return list;
            }
        }).cancelled();

    }

    public void fetchMatchWithTurns(String matchId, IBackendResponse<MatchEntity> callback) {
        (new HttpResponseHandler<MatchEntity>(callback) {
            @Override
            MatchEntity parseJsonResponse(JsonValue json) {
                return new MatchEntity(json);
            }
        }).cancelled();
    }

    public void postMatchStartPlayingTurn(String matchId, IBackendResponse<String> callback) {
        (new HttpResponseHandler<String>(callback) {
            @Override
            String parseJsonResponse(JsonValue json) {
                return json.getString("yourTurnKey");
            }
        }).cancelled();
    }

    public void postMatchPlayedTurn(MatchTurnRequestInfo playedTurnToUpload, IBackendResponse<MatchEntity>
            callback) {
        (new HttpResponseHandler<MatchEntity>(callback) {
            @Override
            MatchEntity parseJsonResponse(JsonValue json) {
                return new MatchEntity(json);
            }
        }).cancelled();
    }

    public void postMatchGiveUp(String matchId, IBackendResponse<MatchEntity> callback) {
        (new HttpResponseHandler<MatchEntity>(callback) {
            @Override
            MatchEntity parseJsonResponse(JsonValue json) {
                return new MatchEntity(json);
            }
        }).cancelled();
    }

    public void postMatchAccepted(String matchId, boolean accepted, IBackendResponse<MatchEntity> callback) {
        (new HttpResponseHandler<MatchEntity>(callback) {
            @Override
            MatchEntity parseJsonResponse(JsonValue json) {
                return new MatchEntity(json);
            }
        }).cancelled();
    }

    protected Net.HttpRequest buildRequest(String uri, @Nullable Map<String, String> params) {
        if (!uri.startsWith("/"))
            uri = "/" + uri;

        final Net.HttpRequest http = new Net.HttpRequest();
        String paramString = params != null ? HttpParametersUtils.convertHttpParameters(params) : "";
        if (paramString.length() > 0)
            uri = uri + "?" + paramString;

        http.setUrl(BASE_URL + uri);
        Gdx.app.debug(LOG_TAG, uri);
        return http;
    }

    public interface IBackendResponse<T> {
        void onFail(int statusCode, String errorMsg);

        void onSuccess(T retrievedData);
    }

    public class PlayerCreatedInfo {
        public final String userId;
        public final String userKey;
        public final String nickName;

        public PlayerCreatedInfo(JsonValue fromJson) {
            userId = fromJson.getString("id");
            userKey = fromJson.getString("secret");
            nickName = fromJson.getString("nickname");
            Gdx.app.log(LOG_TAG, "Player created with nickname " + nickName);
        }
    }

    private abstract class HttpResponseHandler<T> implements Net.HttpResponseListener {
        private final IBackendResponse<T> callback;

        public HttpResponseHandler(IBackendResponse<T> callback) {
            this.callback = callback;
        }

        abstract T parseJsonResponse(JsonValue json);

        @Override
        public void handleHttpResponse(Net.HttpResponse httpResponse) {
            String result = httpResponse.getResultAsString();
            int statusCode = httpResponse.getStatus().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                try {
                    JsonValue response = new JsonReader().parse(result);
                    if (callback != null)
                        callback.onSuccess(parseJsonResponse(response));
                } catch (Throwable t) {
                    Gdx.app.log(LOG_TAG, "Could not parse answer: " + result, t);
                    if (callback != null)
                        callback.onFail(statusCode, "Server error.");
                }
            } else {
                Gdx.app.log(LOG_TAG, statusCode + ": " + result);
                if (callback != null) {
                    String errorMsg = result;
                    if (statusCode > 0 && (errorMsg == null || errorMsg.isEmpty()))
                        errorMsg = "Server returned error " + String.valueOf(statusCode);
                    else if (statusCode <= 0 && errorMsg == null) {
                        statusCode = SC_NO_CONNECTION;
                        errorMsg = "Connection problem";
                    }

                    callback.onFail(statusCode, errorMsg);
                }
            }
        }

        @Override
        public void failed(Throwable t) {
            Gdx.app.error(LOG_TAG, t.getMessage(), t);
            callback.onFail(SC_NO_CONNECTION, t.getMessage());
        }

        @Override
        public void cancelled() {
            callback.onFail(SC_NO_CONNECTION, "Connection problem");
        }
    }

    private class PlayerCreatedResponseHandler extends HttpResponseHandler<PlayerCreatedInfo> {

        public PlayerCreatedResponseHandler(IBackendResponse<PlayerCreatedInfo> callback) {
            super(callback);
        }

        @Override
        PlayerCreatedInfo parseJsonResponse(JsonValue json) {
            PlayerCreatedInfo playerCreatedInfo = new PlayerCreatedInfo(json);
            setUserId(playerCreatedInfo.userId);
            setUserPass(playerCreatedInfo.userKey);
            return playerCreatedInfo;
        }
    }
}
