package info.smart_tools.smartactors.plugin.encoder;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.encoding.codec.Base64;
import info.smart_tools.smartactors.core.encoding.codec.CharSequenceCodec;
import info.smart_tools.smartactors.core.encoding.codec.Hex;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.security.encoding.MDPasswordEncoder;
import info.smart_tools.smartactors.core.security.encoding.codecs.ICharSequenceCodec;
import info.smart_tools.smartactors.core.security.encoding.codecs.ICodec;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, Keys.class, PasswordEncoderPlugin.class, ApplyFunctionToArgumentsStrategy.class, Hex.class})
@RunWith(PowerMockRunner.class)
public class PasswordEncoderPluginTest {

    private PasswordEncoderPlugin plugin;
    private IBootstrap bootstrap;
    private ArgumentCaptor<IPoorAction> actionArgumentCaptor;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Hex.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new PasswordEncoderPlugin(bootstrap);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("PasswordEncoderPlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("PasswordEncoderPlugin");

        actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).before("configure");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());
        verify(bootstrap).add(bootstrapItem);
    }

    @Test
    public void ShouldCorrectLoadPluginAndResolveHex() throws Exception {

        IKey hexEncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("HexEncoder")).thenReturn(hexEncoderKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd("HexEncoder");

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(hexEncoderKey), argumentCaptor.capture());

        argumentCaptor.getValue().resolve();

        verifyStatic();
        Hex.create();
    }

    @Test
    public void ShouldCorrectLoadPluginAndResolveBase64() throws Exception {

        IKey base64EncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("Base64Encoder")).thenReturn(base64EncoderKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd("Base64Encoder");

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(base64EncoderKey), argumentCaptor.capture());

        argumentCaptor.getValue().resolve();

        verifyStatic();
        Base64.create();
    }

    @Test
    public void ShouldCorrectLoadPluginAndResolveCharset() throws Exception {

        IKey charsetKey = mock(IKey.class);
        when(Keys.getOrAdd("CharSequenceCodec")).thenReturn(charsetKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd("CharSequenceCodec");

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(charsetKey), argumentCaptor.capture());

        String charset = "UTF-8";
        argumentCaptor.getValue().resolve(charset);

        verifyStatic();
        CharSequenceCodec.create(charset);
    }

    @Test
    public void ShouldCorrectLoadPluginAndResolvePasswordEncoder() throws Exception {

        IKey passwordEncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("PasswordEncoder")).thenReturn(passwordEncoderKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd("PasswordEncoder");

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(passwordEncoderKey), argumentCaptor.capture());

        String algorithm = "sha-512";
        String encoder = "Base64Encoder";
        String charset = "UTF-8";

        IKey charsetKey = mock(IKey.class);
        when(Keys.getOrAdd("CharSequenceCodec")).thenReturn(charsetKey);
        ICharSequenceCodec charsetCodec = mock(ICharSequenceCodec.class);
        when(IOC.resolve(charsetKey, charset)).thenReturn(charsetCodec);

        IKey encoderKey = mock(IKey.class);
        when(Keys.getOrAdd(encoder)).thenReturn(encoderKey);
        ICodec codec = mock(ICodec.class);
        when(IOC.resolve(encoderKey)).thenReturn(codec);

        argumentCaptor.getValue().resolve(algorithm, encoder, charset);

        verifyStatic();
        MDPasswordEncoder.create(algorithm, codec, charsetCodec);
    }

    @Test(expected = PluginException.class)
    public void ShouldThrowException_When_InternalExceptionIsThrown() throws Exception {

        whenNew(BootstrapItem.class).withArguments("PasswordEncoderPlugin").thenThrow(new InvalidArgumentException(""));
        plugin.load();
    }
}
